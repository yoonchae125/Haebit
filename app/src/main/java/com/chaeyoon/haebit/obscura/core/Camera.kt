package com.chaeyoon.haebit.obscura.core

import com.chaeyoon.haebit.obscura.view.AutoFitSurfaceView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface Camera {
    val aperture: Float
    val isoFlow: StateFlow<Float>
    val shutterSpeedFlow: StateFlow<Float>
    val exposureValueFlow: StateFlow<Float>
    val lockStateFlow: StateFlow<LockState>
    val vibrateFlow: SharedFlow<Unit>
    // for debug
    val lensFocusDistanceFlow: StateFlow<Float>

    fun setOutView(outView: AutoFitSurfaceView, onCameraOpenFailed: () -> Unit)

    fun startCamera()

    fun closeCamera()

    fun lock(x: Float, y: Float)

    fun unLock(needVibrate:Boolean = true)
}