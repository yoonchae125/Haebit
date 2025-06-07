package com.chaeyoon.haebit.obscura.utils.extensions

import com.chaeyoon.haebit.obscura.model.CameraValue

internal fun Float.nearest(among: List<CameraValue>): CameraValue {
    val closestCameraValue = among.minByOrNull {
        kotlin.math.abs(
            if (it.isFraction) {
                1 / it.value
            } else {
                it.value
            } - this
        )
    }
    return closestCameraValue ?: among.firstOrNull() ?: error("cannot be null")
}

internal fun Float.toTwoDecimalPlaces(): Float {
    return (this * 100).toInt() / 100f
}

internal fun Float.toOneDecimalPlaces(): Float {
    return (this * 10).toInt() / 10f
}

internal fun Float.roundToDecimalPlaces(decimalPlaces: Int): Float {
    return when (decimalPlaces) {
        1 -> (this * 10).toInt() / 10f
        2 -> (this * 100).toInt() / 100f
        else -> this
    }
}

internal fun Float.hasDecimalPart(): Boolean {
    return this % 1 != 0f
}