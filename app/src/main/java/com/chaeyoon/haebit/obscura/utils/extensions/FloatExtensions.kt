package com.chaeyoon.haebit.obscura.utils.extensions

internal fun Float.nearest(among: List<Float>): Float {
    return among.minByOrNull { kotlin.math.abs(it - this) } ?: this
}

internal fun Float.toTwoDecimalPlaces(): Float {
    return (this * 100).toInt() / 100f
}

internal fun Float.toOneDecimalPlaces(): Float {
    return (this * 10).toInt() / 10f
}