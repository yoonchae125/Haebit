package com.chaeyoon.haebit.obscura.utils.extensions

import android.graphics.RectF
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import android.hardware.camera2.params.MeteringRectangle
import android.util.Log
import androidx.core.graphics.toRect
import com.chaeyoon.haebit.obscura.utils.CameraCoordinateTransformer

private const val TAG = "CameraExtensions"
internal fun CameraCoordinateTransformer.getTouchLockRegion(
    x: Float,
    y: Float,
    size: Int,
    maxWidth: Int,
    maxHeight: Int
): MeteringRectangle {

    val touchRect = RectF(
        (x - size).coerceIn(0f, maxWidth.toFloat()),
        (y - size).coerceIn(0f, maxHeight.toFloat()),
        (x + size).coerceIn(0f, maxWidth.toFloat()),
        (y + size).coerceIn(0f, maxHeight.toFloat())
    )
    val lockRect = toCameraSpace(touchRect)

    Log.d(TAG, "touch$touchRect")
    Log.d(TAG, "lock$lockRect")

    return MeteringRectangle(
        lockRect.toRect(),
        MeteringRectangle.METERING_WEIGHT_MAX - 1
    )
}

internal fun TotalCaptureResult.isFocused(): Boolean {
    val afState = get(CaptureResult.CONTROL_AF_STATE)

    return afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED ||
            afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED
}

internal fun TotalCaptureResult.isExposureConverged(): Boolean {
    val aeState = get(CaptureResult.CONTROL_AE_STATE)

    return aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED
}

internal fun CameraCharacteristics.isMeteringAreaAFSupported() =
    (get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF) ?: 0) >= 1

internal fun CameraCharacteristics.isMeteringAreaAESupported() =
    (get(CameraCharacteristics.CONTROL_MAX_REGIONS_AE) ?: 0) > 0