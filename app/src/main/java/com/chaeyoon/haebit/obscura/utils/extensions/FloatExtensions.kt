package com.chaeyoon.haebit.obscura.utils.extensions

import com.chaeyoon.haebit.obscura.utils.constants.CameraValue

internal fun Float.nearest(among: List<CameraValue>): CameraValue {
    val closestCameraValue = among.minByOrNull { kotlin.math.abs(it.value - this) }
    return closestCameraValue ?: among.firstOrNull()?: error("cannot be null")
}

internal fun Float.toTwoDecimalPlaces(): Float {
    return (this * 100).toInt() / 100f
}

internal fun Float.toOneDecimalPlaces(): Float {
    return (this * 10).toInt() / 10f
}