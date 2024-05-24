package com.chaeyoon.haebit.obscura.core

import com.chaeyoon.haebit.obscura.view.AutoFitSurfaceView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

interface Camera {
    val aperture: Float
    val isoFlow: StateFlow<Int>
    val shutterSpeedFlow: StateFlow<Float>
    val exposureValueFlow: StateFlow<Float>

    fun setOutView(outView: AutoFitSurfaceView, onCameraOpenFailed:()->Unit)

    fun startCamera(coroutineScope: CoroutineScope)
}