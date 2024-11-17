package com.chaeyoon.haebit.util

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import com.chaeyoon.haebit.obscura.utils.extensions.getVibrator

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

    companion object {
        private const val VIBRATE_DURATION = 2L
        private const val MAX_AMPLITUDE = 255
    }
}