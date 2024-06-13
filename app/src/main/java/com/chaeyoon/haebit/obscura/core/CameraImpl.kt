package com.chaeyoon.haebit.obscura.core

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import com.chaeyoon.haebit.lightmeter.LightMeterCalculator
import com.chaeyoon.haebit.lightmeter.functions.nanoSecondsToSeconds
import com.chaeyoon.haebit.obscura.view.AutoFitSurfaceView
import com.chaeyoon.haebit.obscura.utils.functions.getPreviewOutputSize
import com.google.android.gms.common.util.concurrent.HandlerExecutor
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class CameraImpl private constructor(context: Context) : Camera {
    // camera values
    private var mutableAperture = 0f
    private val isoMutableFlow = MutableStateFlow<Float>(0f)
    private val shutterSpeedMutableFlow = MutableStateFlow<Float>(0f)
    private val exposureValueMutableFlow = MutableStateFlow<Float>(0f)
    override val aperture: Float
        get() = mutableAperture
    override val isoFlow: StateFlow<Float> = isoMutableFlow.asStateFlow()
    override val shutterSpeedFlow: StateFlow<Float> = shutterSpeedMutableFlow.asStateFlow()
    override val exposureValueFlow: StateFlow<Float> = exposureValueMutableFlow.asStateFlow()
    private val lightMeterCalculator = LightMeterCalculator()

    // camera
    private val cameraManager: CameraManager =
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private lateinit var cameraId: String
    private val cameraThread = HandlerThread("CameraThread").apply { start() }
    private val cameraHandler = Handler(cameraThread.looper)
    private var outView: AutoFitSurfaceView? = null
    private lateinit var onCameraOpenFailed: () -> Unit

    override fun setOutView(outView: AutoFitSurfaceView, onCameraOpenFailed: () -> Unit) {
        this.outView = outView
        this.onCameraOpenFailed = onCameraOpenFailed
        getCameraId()?.let { cameraId = it } ?: run { onCameraOpenFailed() }

        val characteristics = cameraManager.getCameraCharacteristics(cameraId)

        outView.holder.addCallback(object : SurfaceHolder.Callback {
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
                    outView.display,
                    characteristics,
                    ImageFormat.YUV_420_888
                )

                outView.setAspectRatio(
                    previewSize.width,
                    previewSize.height
                )
            }
        })
    }


    override fun startCamera(coroutineScope: CoroutineScope) {
        outView?.rootView?.post {
            internalStartCamera(coroutineScope)
        }
    }

    private fun internalStartCamera(coroutineScope: CoroutineScope) =
        coroutineScope.launch(Dispatchers.Main) {
            val nonNullView = requireNotNull(outView)
            val camera = openCamera(cameraId, onCameraOpenFailed)

            // Creates list of Surfaces where the camera will output frames
            val targets = listOf(nonNullView.holder.surface)

            // Start a capture session using our open camera and list of Surfaces where frames will go
            val session = createCaptureSession(camera, targets)

            val captureRequestBuilder = camera.createCaptureRequest(
                CameraDevice.TEMPLATE_PREVIEW
            ).apply { addTarget(nonNullView.holder.surface) }

            mutableAperture = captureRequestBuilder.get(CaptureRequest.LENS_APERTURE)!!

            // This will keep sending the capture request as frequently as possible until the
            // session is torn down or session.stopRepeating() is called
            session.setRepeatingRequest(captureRequestBuilder.build(), object :
                CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    result: TotalCaptureResult
                ) {
                    super.onCaptureCompleted(session, request, result)

                    updateCameraValues(result)
                }
            }, cameraHandler)
        }

    private fun updateCameraValues(result: CaptureResult) {
        val iso = result.get(CaptureResult.SENSOR_SENSITIVITY)!!
        val shutterSpeed = nanoSecondsToSeconds(result.get(CaptureResult.SENSOR_EXPOSURE_TIME)!!)
        val exposureValue =
            lightMeterCalculator.calculateExposureValue(aperture, shutterSpeed, iso.toFloat())

        isoMutableFlow.update { iso.toFloat() }
        shutterSpeedMutableFlow.update { shutterSpeed }
        exposureValueMutableFlow.update { exposureValue }

        Log.d(TAG, "aperture: $aperture")
        Log.d(TAG, "iso: $iso")
        Log.d(TAG, "shutter speed: $shutterSpeed")
        Log.d(TAG, "exposure value: $exposureValue")
    }

    private fun getCameraId(): String? {
        return cameraManager.cameraIdList
            .find {
                val characteristics = cameraManager.getCameraCharacteristics(it)
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
            CameraStateCallbackImpl(onCameraOpenFailed, cont),
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


    companion object {
        private const val TAG = "CameraImpl"
        private var instance: CameraImpl? = null
        fun getInstance(context: Context): CameraImpl {
            return instance ?: synchronized(this) {
                CameraImpl(context).also {
                    instance = it
                }
            }
        }
    }

    inner class CameraStateCallbackImpl(
        private val onCameraOpenFailed: () -> Unit,
        private val cont: CancellableContinuation<CameraDevice>
    ) : CameraDevice.StateCallback() {
        override fun onOpened(device: CameraDevice) = cont.resume(device)

        override fun onDisconnected(device: CameraDevice) {
            Log.w(TAG, "Camera has been disconnected")
            onCameraOpenFailed()
        }

        override fun onError(device: CameraDevice, error: Int) {
            val msg = when (error) {
                ERROR_CAMERA_DEVICE -> "Fatal (device)"
                ERROR_CAMERA_DISABLED -> "Device policy"
                ERROR_CAMERA_IN_USE -> "Camera in use"
                ERROR_CAMERA_SERVICE -> "Fatal (service)"
                ERROR_MAX_CAMERAS_IN_USE -> "Maximum cameras in use"
                else -> "Unknown"
            }
            val exc = RuntimeException("Camera error: ($error) $msg")
            Log.e(TAG, exc.message, exc)
            if (cont.isActive) cont.resumeWithException(exc)
        }
    }
}