package com.chaeyoon.haebit.obscura.core

import com.chaeyoon.haebit.obscura.view.AutoFitSurfaceView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

interface Camera {
    val aperture: Float
    val isoFlow: StateFlow<Float>
    val shutterSpeedFlow: StateFlow<Float>
    val exposureValueFlow: StateFlow<Float>
    val isLockedFlow: StateFlow<Boolean>
    // for debug
    val lensFocusDistanceFlow: StateFlow<Float>

    fun setOutView(outView: AutoFitSurfaceView, onCameraOpenFailed: () -> Unit)

    fun startCamera(coroutineScope: CoroutineScope)

    fun lock(x: Float, y: Float, coroutineScope: CoroutineScope)

    fun unLock()
}