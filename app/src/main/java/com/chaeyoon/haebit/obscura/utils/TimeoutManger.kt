package com.chaeyoon.haebit.obscura.utils

import android.os.SystemClock
import android.util.Log

class TimeoutManger(private val timeoutMs: Long) {
    private var captureTimer: Long = 0L

    fun startTimerLocked() {
        captureTimer = SystemClock.elapsedRealtime()
    }

    fun hitTimeoutLocked(): Boolean {
        val leftTime = SystemClock.elapsedRealtime() - captureTimer
        val timeout = leftTime > timeoutMs
        if (timeout) {
            Log.d(TAG, "time out")
        }
        return timeout
    }

    companion object {
        private const val TAG = "TimeoutManger"
    }
}