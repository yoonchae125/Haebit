package com.chaeyoon.haebit.permission

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat.startActivity
import com.chaeyoon.haebit.R

class CameraPermissionDeniedAlertDialog(context: Context) {
    private val dialogBuilder = AlertDialog.Builder(context)
        .setMessage(R.string.alert_camera_permission_denied_message)
        .setTitle(R.string.alert_camera_permission_denied_title)
        .setPositiveButton(R.string.alert_camera_permission_denied_button) { dialog, _ ->
            try {
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).also { intent ->
                    intent.data = Uri.parse("package:$PACKAGE_NAME")
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(context, intent, null)
                }
            } catch (e: ActivityNotFoundException) {
                Log.e(TAG, e.stackTraceToString())
            }
            dialog.dismiss()
        }
        .setCancelable(false)
        .create()

    fun show() {
        dialogBuilder.show()
    }

    companion object {
        private val TAG = this::class.simpleName
        private const val PACKAGE_NAME = "com.chaeyoon.haebit"
    }
}