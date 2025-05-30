package com.chaeyoon.haebit.lightmeter

import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.sqrt

class LightMeterCalculator {
    fun calculateExposureValue(
        aperture: Float,
        shutterSpeed: Float,
        iso: Float
    ): Float =
        log2((100 * aperture.pow(2)) / (iso * shutterSpeed))

    fun calculateIsoValue(
        ev: Float,
        shutterSpeed: Float,
        aperture: Float
    ): Float {
        return (100 * aperture.pow(2)) / (shutterSpeed * 2f.pow(ev))
    }


    fun calculateShutterSpeedValue(
        ev: Float,
        iso: Float,
        aperture: Float
    ): Float {
        return (100 * aperture.pow(2)) / (iso * 2f.pow(ev))
    }

    fun calculateApertureValue(
        ev: Float,
        iso: Float,
        shutterSpeed: Float
    ): Float {
        return sqrt((iso * shutterSpeed * 2f.pow(ev)) / 100f)
    }
}