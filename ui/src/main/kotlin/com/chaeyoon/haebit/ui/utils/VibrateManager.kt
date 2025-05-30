package com.chaeyoon.haebit.ui.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class VibrateManager(context: Context) {
    private val vibrator = context.getVibrator()

    fun vibrate() {
        val vibrator = vibrator?.takeIf(Vibrator::hasVibrator)
            ?: return

        val effect = VibrationEffect.createOneShot(
            VIBRATE_DURATION,
            MAX_AMPLITUDE
        )
        vibrator.vibrate(effect)
    }

    private fun Context.getVibrator(): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            getSystemService(Vibrator::class.java)
        }
    }
    companion object {
        private const val VIBRATE_DURATION = 2L
        private const val MAX_AMPLITUDE = 255
    }
}