package com.chaeyoon.haebit.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.chaeyoon.haebit.utils.CameraPermissionDeniedAlertDialog

class PermissionChecker(fragment: Fragment) {
    private val context = fragment.requireContext()
    
    private val permissionLauncher = fragment.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultMap ->
        val deniedPermissions = resultMap.filter {
            !it.value
        }
        showPermissionErrorToast(context, deniedPermissions)
    }

    fun checkCameraPermissions(): Boolean {
        val cameraPermissionsGranted = isPermissionGranted(CAMERA_PERMISSION)

        return if (cameraPermissionsGranted) {
            true
        } else {
            val targetPermissions = arrayOf(CAMERA_PERMISSION)
            permissionLauncher.launch(targetPermissions)
            false
        }
    }

    private fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun showPermissionErrorToast(
        context: Context,
        deniedPermissions: Map<String, Boolean>
    ) {
        when {
            deniedPermissions.contains(CAMERA_PERMISSION) -> {
                showCameraPermissionErrorToast(context)
            }
        }
    }

    private fun showCameraPermissionErrorToast(context: Context) {
        CameraPermissionDeniedAlertDialog(context).show()
    }

    companion object {
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
    }
}