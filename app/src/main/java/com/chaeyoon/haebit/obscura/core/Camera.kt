package com.chaeyoon.haebit.obscura.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

interface Camera {
    val aperture: Float?
    val isoFlow: StateFlow<Int>
    val shutterSpeedFlow: StateFlow<Float>
    val exposureValueFlow: StateFlow<Float>

    fun startCamera(coroutineScope: CoroutineScope)
}