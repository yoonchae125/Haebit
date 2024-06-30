package com.chaeyoon.haebit.obscura.core

import android.hardware.camera2.CameraDevice
import android.util.Log
import kotlinx.coroutines.CancellableContinuation
import kotlin.coroutines.resume

class CameraStateCallback(
    private val onCameraOpenFailed: () -> Unit,
    private val cont: CancellableContinuation<CameraDevice>
) : CameraDevice.StateCallback() {
    override fun onOpened(device: CameraDevice) = cont.resume(device)

    override fun onDisconnected(device: CameraDevice) {
        Log.w(TAG, "Camera has been disconnected")
        device.close()
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
        Log.e(TAG, msg)
        device.close()
        onCameraOpenFailed()
    }

    companion object{
        const val TAG = "CameraStateCallback"
    }

}