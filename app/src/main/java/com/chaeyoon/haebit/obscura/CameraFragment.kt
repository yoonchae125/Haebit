package com.chaeyoon.haebit.obscura

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.hardware.camera2.params.SessionConfiguration.SESSION_REGULAR
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.LayoutInflater
import android.view.Surface
import android.view.SurfaceHolder
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.chaeyoon.haebit.R
import com.chaeyoon.haebit.databinding.FragmentCameraBinding
import com.chaeyoon.haebit.permission.PermissionChecker
import com.chaeyoon.haebit.utils.functions.getPreviewOutputSize
import com.google.android.gms.common.util.concurrent.HandlerExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * CameraFragment
 */
class CameraFragment : Fragment(R.layout.fragment_camera) {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private lateinit var permissionChecker: PermissionChecker

    // Camera
    private val cameraManager: CameraManager by lazy {
        val context = requireContext().applicationContext
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }
    private lateinit var camera: CameraDevice
    private lateinit var cameraId: String
    private lateinit var characteristics: CameraCharacteristics
    private lateinit var session: CameraCaptureSession
    private val cameraThread = HandlerThread("CameraThread").apply { start() }
    private val cameraHandler = Handler(cameraThread.looper)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)

        initPermissionChecker()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initCameraView()
    }

    override fun onStart() {
        super.onStart()
        permissionChecker.checkCameraPermissions()
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    private fun initPermissionChecker() {
        permissionChecker = PermissionChecker(this)
    }


    private fun initCameraView() {
        cameraId = getCameraId() ?: run {
            // TODO: handle error
            requireActivity().finish()
            return
        }

        characteristics = cameraManager.getCameraCharacteristics(cameraId)

        binding.cameraPreview.holder.addCallback(object : SurfaceHolder.Callback {
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
                    binding.cameraPreview.display,
                    characteristics,
                    ImageFormat.YUV_420_888
                )

                binding.cameraPreview.setAspectRatio(
                    previewSize.width,
                    previewSize.height
                )

                // To ensure that size is set, initialize camera in the view's thread
                binding.root.post { initCamera() }
            }
        })
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

    private fun initCamera() = lifecycleScope.launch(Dispatchers.Main) {
        // Open the selected camera
        camera = openCamera(cameraManager, cameraId, cameraHandler)


        // Creates list of Surfaces where the camera will output frames
        val targets = listOf(binding.cameraPreview.holder.surface)

        // Start a capture session using our open camera and list of Surfaces where frames will go
        session = createCaptureSession(camera, targets, cameraHandler)

        val captureRequest = camera.createCaptureRequest(
            CameraDevice.TEMPLATE_PREVIEW
        ).apply { addTarget(binding.cameraPreview.holder.surface) }

        // This will keep sending the capture request as frequently as possible until the
        // session is torn down or session.stopRepeating() is called
        session.setRepeatingRequest(captureRequest.build(), null, cameraHandler)
    }

    @SuppressLint("MissingPermission")
    private suspend fun openCamera(
        manager: CameraManager,
        cameraId: String,
        handler: Handler? = null
    ): CameraDevice = suspendCancellableCoroutine { cont ->
        manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(device: CameraDevice) = cont.resume(device)

            override fun onDisconnected(device: CameraDevice) {
                Log.w(TAG, "Camera $cameraId has been disconnected")
                requireActivity().finish()
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
                val exc = RuntimeException("Camera $cameraId error: ($error) $msg")
                Log.e(TAG, exc.message, exc)
                if (cont.isActive) cont.resumeWithException(exc)
            }
        }, handler)
    }

    private suspend fun createCaptureSession(
        device: CameraDevice,
        targets: List<Surface>,
        handler: Handler
    ): CameraCaptureSession = suspendCoroutine { cont ->
        val stateCallback = object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) =
                cont.resume(session)

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
                    SESSION_REGULAR,
                    outputs,
                    HandlerExecutor(handler.looper),
                    stateCallback
                )
            )
        } else {
            device.createCaptureSession(
                targets,
                stateCallback,
                handler
            )
        }
    }

    companion object {
        private val TAG = CameraFragment::class.java.simpleName
    }
}