package com.chaeyoon.haebit.obscura.utils.functions

import android.annotation.SuppressLint
import android.graphics.Point
import android.hardware.camera2.CameraCharacteristics
import android.util.Size
import android.view.Display
import kotlin.math.max
import kotlin.math.min

/** Helper class used to pre-compute shortest and longest sides of a [Size] */
class SmartSize(width: Int, height: Int) {
    var size = Size(width, height)
    var long = max(size.width, size.height)
    var short = min(size.width, size.height)
    override fun toString() = "SmartSize(${long}x${short})"
}

/** Standard High Definition size for pictures and video */
val SIZE_1080P: SmartSize = SmartSize(1920, 1080)

/** Returns a [SmartSize] object for the given [Display] */
fun getDisplaySmartSize(display: Display): SmartSize {
    val outPoint = Point()
    display.getRealSize(outPoint)
    return SmartSize(outPoint.x, outPoint.y)
}

/**
 * Returns the largest available PREVIEW size. For more information, see:
 * https://d.android.com/reference/android/hardware/camera2/CameraDevice and
 * https://developer.android.com/reference/android/hardware/camera2/params/StreamConfigurationMap
 */
@SuppressLint("SuspiciousIndentation")
fun getPreviewOutputSize(
    display: Display,
    characteristics: CameraCharacteristics,
    format: Int
): Size {

    // Find which is smaller: screen or 1080p
    val screenSize = getDisplaySmartSize(display)
    val hdScreen = screenSize.long >= SIZE_1080P.long || screenSize.short >= SIZE_1080P.short
    val maxSize = if (hdScreen) SIZE_1080P else screenSize

    // If image format is provided, use it to determine supported sizes; else use target class
    val config = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
    assert(config.isOutputSupportedFor(format))
    val allSizes = config.getOutputSizes(format)

    // Get available sizes and sort them by area from largest to smallest
    val validSizes = allSizes
        .sortedWith(compareBy { it.height * it.width })
        .map { SmartSize(it.width, it.height) }.reversed()

    // Then, get the largest output size that is smaller or equal than our max size
    return validSizes.first { it.long <= maxSize.long && it.short <= maxSize.short }.size
}