package com.chaeyoon.haebit.obscura.core

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.RectF
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCaptureSession.CaptureCallback
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureFailure
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import android.hardware.camera2.params.MeteringRectangle
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import com.chaeyoon.lightmeter.LightMeterCalculator
import com.chaeyoon.haebit.functions.nanoSecondsToSeconds
import com.chaeyoon.haebit.obscura.utils.CameraCoordinateTransformer
import com.chaeyoon.haebit.obscura.utils.TimeoutManger
import com.chaeyoon.haebit.obscura.utils.extensions.getTouchLockRegion
import com.chaeyoon.haebit.obscura.utils.extensions.isExposureConverged
import com.chaeyoon.haebit.obscura.utils.extensions.isFocused
import com.chaeyoon.haebit.obscura.utils.extensions.isMeteringAreaAESupported
import com.chaeyoon.haebit.obscura.utils.extensions.isMeteringAreaAFSupported
import com.chaeyoon.haebit.obscura.utils.functions.getPreviewOutputSize
import com.chaeyoon.haebit.obscura.utils.functions.logAEState
import com.chaeyoon.haebit.obscura.utils.functions.logAFState
import com.chaeyoon.haebit.obscura.view.AutoFitSurfaceView
import com.google.android.gms.common.util.concurrent.HandlerExecutor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class CameraImpl private constructor(
    context: Context,
    private val coroutineScope: CoroutineScope
) : Camera {
    // camera values
    private var mutableAperture = 0f
    private val isoMutableFlow = MutableStateFlow(0f)
    private val shutterSpeedMutableFlow = MutableStateFlow(0f)
    private val exposureValueMutableFlow = MutableStateFlow(0f)
    override val aperture: Float
        get() = mutableAperture
    override val isoFlow: StateFlow<Float> = isoMutableFlow.asStateFlow()
    override val shutterSpeedFlow: StateFlow<Float> = shutterSpeedMutableFlow.asStateFlow()
    override val exposureValueFlow: StateFlow<Float> = exposureValueMutableFlow.asStateFlow()

    private val mutableVibrateFlow = MutableSharedFlow<Unit>()
    override val vibrateFlow: SharedFlow<Unit> = mutableVibrateFlow.asSharedFlow()

    private val lightMeterCalculator = com.chaeyoon.lightmeter.LightMeterCalculator()

    // debug
    private val lensFocusDistanceMutableFlow = MutableStateFlow(0f)
    override val lensFocusDistanceFlow: StateFlow<Float> =
        lensFocusDistanceMutableFlow.asStateFlow()
    private val lockStateMutableFlow = MutableStateFlow(LockState.UNLOCKED)
    override val lockStateFlow: StateFlow<LockState> =
        lockStateMutableFlow.asStateFlow()

    // camera
    private var isCameraOpened = false
    private var camera: CameraDevice? = null
    private val cameraManager: CameraManager =
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private lateinit var cameraId: String
    private var captureSession: CameraCaptureSession? = null
    private lateinit var characteristics: CameraCharacteristics

    private val cameraThread = HandlerThread("CameraThread").apply { start() }
    private val cameraHandler = Handler(cameraThread.looper)

    private var surfaceView: AutoFitSurfaceView? = null
    private lateinit var onCameraOpenFailed: () -> Unit
    private lateinit var previewRequestBuilder: CaptureRequest.Builder

    private var touchLockRegion = MeteringRectangle(0, 0, 0, 0, 0)
    private val timeoutManger = TimeoutManger(PRECAPTURE_TIMEOUT_MS)
    private lateinit var cameraCoordinateTransformer: CameraCoordinateTransformer

    override fun setOutView(outView: AutoFitSurfaceView, onCameraOpenFailed: () -> Unit) {
        this.surfaceView = outView
        this.onCameraOpenFailed = onCameraOpenFailed

        getCameraId()?.let { cameraId = it } ?: run { onCameraOpenFailed() }

        addSurfaceHolderCallback(outView)

        outView.post {
            initCoordinateTransformer(outView)
        }
    }

    private fun addSurfaceHolderCallback(surfaceView: AutoFitSurfaceView) {
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceDestroyed(holder: SurfaceHolder) = Unit

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) = Unit

            override fun surfaceCreated(holder: SurfaceHolder) {
                // Selects appropriate preview size and configures view finder
                val previewSize = getPreviewOutputSize(
                    surfaceView.display,
                    characteristics,
                    ImageFormat.YUV_420_888
                )

                surfaceView.setAspectRatio(
                    previewSize.width,
                    previewSize.height
                )
            }
        })

    }

    private fun initCoordinateTransformer(surfaceView: AutoFitSurfaceView) {
        val surfaceRect =
            RectF(0f, 0f, surfaceView.width.toFloat(), surfaceView.height.toFloat())
        cameraCoordinateTransformer =
            CameraCoordinateTransformer(characteristics, surfaceRect)
    }

    override fun startCamera() {
        surfaceView?.rootView?.post {
            internalStartCamera(coroutineScope)
        }
    }

    override fun closeCamera() {
        camera?.close()
        camera = null
        isCameraOpened = false
    }

    override fun lock(x: Float, y: Float) {
        if (!isCameraOpened) return

        coroutineScope.launch {
            mutableVibrateFlow.emit(Unit)
        }
        unLock()

        lockStateMutableFlow.update { LockState.LOCK_PROCESSING }

        captureSession?.stopRepeating()

        setLockRegion(characteristics, x, y)

        setTriggerLock()

        timeoutManger.startTimerLocked()

        captureSession?.capture(
            previewRequestBuilder.build(),
            createLockCaptureCallback(),
            cameraHandler
        )
    }

    private fun setTriggerLock() {
        previewRequestBuilder.set(
            CaptureRequest.CONTROL_AF_MODE,
            CaptureRequest.CONTROL_AF_MODE_AUTO
        )
        previewRequestBuilder.set(
            CaptureRequest.CONTROL_AE_MODE,
            CaptureRequest.CONTROL_AE_MODE_ON
        )
        previewRequestBuilder.set(
            CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
            CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START
        )
        previewRequestBuilder.set(
            CaptureRequest.CONTROL_AF_TRIGGER,
            CameraMetadata.CONTROL_AF_TRIGGER_START
        )
    }

    private fun internalLock(result: CaptureResult) {
        val currentAeSensitivity = result.get(CaptureResult.SENSOR_SENSITIVITY)
        val currentAeExposureTime = result.get(CaptureResult.SENSOR_EXPOSURE_TIME)
        val lensFocusDistance = result.get(CaptureResult.LENS_FOCUS_DISTANCE)

        offAutoControlMode()

        if (currentAeSensitivity != null) {
            previewRequestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, currentAeSensitivity)
        }
        if (currentAeExposureTime != null) {
            previewRequestBuilder.set(
                CaptureRequest.SENSOR_EXPOSURE_TIME,
                currentAeExposureTime
            )
        }
        if (lensFocusDistance != null) {
            previewRequestBuilder.set(
                CaptureRequest.LENS_FOCUS_DISTANCE,
                lensFocusDistance
            )
        }

        captureSession?.setRepeatingRequest(
            previewRequestBuilder.build(),
            createCameraCaptureCallback(),
            null
        )

        lockStateMutableFlow.update { LockState.LOCKED }
        coroutineScope.launch {
            mutableVibrateFlow.emit(Unit)
        }
    }

    private fun offAutoControlMode() {
        previewRequestBuilder.set(
            CaptureRequest.CONTROL_AE_MODE,
            CaptureRequest.CONTROL_AE_MODE_OFF
        )
        previewRequestBuilder.set(
            CaptureRequest.CONTROL_AF_MODE,
            CaptureRequest.CONTROL_AF_MODE_OFF
        )
//        previewRequestBuilder.set(
//            CaptureRequest.CONTROL_AE_LOCK,
//            true
//        )
    }

    override fun unLock(needVibrate: Boolean) {
        if (needVibrate) {
            coroutineScope.launch {
                mutableVibrateFlow.emit(Unit)
            }
        }

        captureSession?.stopRepeating()

        cancelTriggerLock()

        onAutoControlMode()

        captureSession?.capture(
            previewRequestBuilder.build(),
            object : CaptureCallback() {
                override fun onCaptureCompleted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    result: TotalCaptureResult
                ) {
                    startCameraPreview()
                    updateCameraValues(result)
                }
            },
            cameraHandler
        )
        lockStateMutableFlow.update { LockState.UNLOCKED }
    }

    private fun cancelTriggerLock() {
        previewRequestBuilder.set(
            CaptureRequest.CONTROL_AF_TRIGGER,
            CaptureRequest.CONTROL_AF_TRIGGER_CANCEL
        )
        previewRequestBuilder.set(
            CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
            CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_CANCEL
        )
    }

    private fun onAutoControlMode() {
        previewRequestBuilder.set(
            CaptureRequest.CONTROL_AF_MODE,
            CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
        )
        previewRequestBuilder.set(
            CaptureRequest.CONTROL_AE_MODE,
            CaptureRequest.CONTROL_AE_MODE_ON
        )
        previewRequestBuilder.set(
            CaptureRequest.CONTROL_AE_LOCK,
            false
        )
    }

    private fun startCameraPreview() {
        captureSession?.setRepeatingRequest(
            previewRequestBuilder.build(),
            createCameraCaptureCallback(),
            cameraHandler
        )
    }

    private fun internalStartCamera(coroutineScope: CoroutineScope) =
        coroutineScope.launch(Dispatchers.Main) {
            val nonNullView = requireNotNull(surfaceView)
            val camera = openCamera(cameraId, onCameraOpenFailed).also {
                camera = it
            }

            // Creates list of Surfaces where the camera will output frames
            val targets = listOf(nonNullView.holder.surface)

            // Start a capture session using our open camera and list of Surfaces where frames will go
            captureSession = createCaptureSession(camera, targets)

            previewRequestBuilder = camera.createCaptureRequest(
                CameraDevice.TEMPLATE_PREVIEW
            ).apply {
                addTarget(nonNullView.holder.surface)
                set(
                    CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON
                )
                set(
                    CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_AUTO
                )
            }

            mutableAperture = previewRequestBuilder.get(CaptureRequest.LENS_APERTURE)!!

            startCameraPreview()

            isCameraOpened = true
        }

    private fun getCameraId(): String? {
        return cameraManager.cameraIdList
            .find {
                characteristics = cameraManager.getCameraCharacteristics(it)
                val capabilities = characteristics.get(
                    CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES
                )
                val outputFormats = characteristics.get(
                    CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
                )?.outputFormats
                val orientation = characteristics.get(CameraCharacteristics.LENS_FACING)
                val isBackwardCompatible = capabilities?.contains(
                    CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_BACKWARD_COMPATIBLE
                ) ?: false
                val isYUB420Format = outputFormats?.contains(ImageFormat.YUV_420_888) ?: false
                val isFacingBack = orientation == CameraCharacteristics.LENS_FACING_BACK

                isBackwardCompatible && isYUB420Format && isFacingBack
            }
    }

    @SuppressLint("MissingPermission")
    private suspend fun openCamera(
        cameraId: String,
        onCameraOpenFailed: () -> Unit
    ): CameraDevice = suspendCancellableCoroutine { cont ->
        cameraManager.openCamera(
            cameraId,
            CameraStateCallback(onCameraOpenFailed, cont),
            cameraHandler
        )
    }

    private suspend fun createCaptureSession(
        device: CameraDevice,
        targets: List<Surface>
    ): CameraCaptureSession = suspendCoroutine { cont ->
        val stateCallback = object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                cont.resume(session)
            }

            override fun onConfigureFailed(session: CameraCaptureSession) {
                val exc =
                    RuntimeException("Camera ${device.id} session configuration failed")
                Log.e(TAG, exc.message, exc)
                cont.resumeWithException(exc)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val outputs = targets.map {
                OutputConfiguration(it)
            }
            device.createCaptureSession(
                SessionConfiguration(
                    SessionConfiguration.SESSION_REGULAR,
                    outputs,
                    HandlerExecutor(cameraHandler.looper),
                    stateCallback
                )
            )
        } else {
            device.createCaptureSession(
                targets,
                stateCallback,
                cameraHandler
            )
        }
    }

    private fun createCameraCaptureCallback() = object : CaptureCallback() {
        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult
        ) {
            updateCameraValues(result)
        }
    }

    private fun createLockCaptureCallback(): CaptureCallback =
        object : CaptureCallback() {
            override fun onCaptureCompleted(
                session: CameraCaptureSession,
                request: CaptureRequest,
                result: TotalCaptureResult
            ) {
                logAEState(TAG, result)
                logAFState(TAG, result)

                val focused = result.isFocused()
                val exposureConverged = result.isExposureConverged()

                if (focused && (timeoutManger.hitTimeoutLocked() || exposureConverged)) {
                    internalLock(result)
                } else {
                    retryLock()
                }

                updateCameraValues(result)
            }

            override fun onCaptureFailed(
                session: CameraCaptureSession,
                request: CaptureRequest,
                failure: CaptureFailure
            ) {
                Log.e(TAG, "Manual AF failure: $failure")
            }
        }

    private fun retryLock() {
        try {
            previewRequestBuilder.set(
                CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_IDLE
            )
            previewRequestBuilder.set(
                CaptureRequest.CONTROL_AF_TRIGGER,
                CaptureRequest.CONTROL_AF_TRIGGER_IDLE
            )

            captureSession?.capture(
                previewRequestBuilder.build(),
                createLockCaptureCallback(),
                cameraHandler
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun setLockRegion(characteristics: CameraCharacteristics, x: Float, y: Float) {
        touchLockRegion = cameraCoordinateTransformer.getTouchLockRegion(
            x,
            y,
            LOCK_REGION_SIZE,
            surfaceView!!.width,
            surfaceView!!.height
        )

        if (characteristics.isMeteringAreaAFSupported()) {
            previewRequestBuilder.set(
                CaptureRequest.CONTROL_AF_REGIONS,
                arrayOf(touchLockRegion)
            )
        }

        if (characteristics.isMeteringAreaAESupported()) {
            previewRequestBuilder.set(
                CaptureRequest.CONTROL_AE_REGIONS,
                arrayOf(touchLockRegion)
            )
        }
    }

    private fun updateCameraValues(result: CaptureResult) {
        val iso = result.get(CaptureResult.SENSOR_SENSITIVITY)!!
        val shutterSpeed =
            nanoSecondsToSeconds(result.get(CaptureResult.SENSOR_EXPOSURE_TIME)!!)
        val exposureValue =
            lightMeterCalculator.calculateExposureValue(
                aperture,
                shutterSpeed,
                iso.toFloat()
            )

        isoMutableFlow.update { iso.toFloat() }
        shutterSpeedMutableFlow.update { shutterSpeed }
        exposureValueMutableFlow.update { exposureValue }

        Log.d(TAG, "aperture: $aperture")
        Log.d(TAG, "iso: $iso")
        Log.d(TAG, "shutter speed: $shutterSpeed")
        Log.d(TAG, "exposure value: $exposureValue")

        // debug
        val lensFocusDistance = result.get(CaptureResult.LENS_FOCUS_DISTANCE)!!
        lensFocusDistanceMutableFlow.update { lensFocusDistance }
        Log.d(TAG, "lens focus distance: $lensFocusDistance")
    }

    companion object {
        private const val TAG = "CameraImpl"
        private const val PRECAPTURE_TIMEOUT_MS = 2000L
        private const val LOCK_REGION_SIZE = 150

        private var instance: CameraImpl? = null
        fun getInstance(context: Context, coroutineScope: CoroutineScope): CameraImpl {
            return instance ?: synchronized(this) {
                CameraImpl(context, coroutineScope).also {
                    instance = it
                }
            }
        }
    }
}