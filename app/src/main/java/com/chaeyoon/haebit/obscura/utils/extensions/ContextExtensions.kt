package com.chaeyoon.haebit.obscura.utils.extensions

import android.content.Context
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager

fun Context.getVibrator(): Vibrator? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        getSystemService(Vibrator::class.java)
    }
}