package com.chaeyoon.haebit.obscura.utils.extensions

import android.graphics.RectF
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import android.hardware.camera2.params.MeteringRectangle
import androidx.core.graphics.toRect
import com.chaeyoon.haebit.obscura.utils.CameraCoordinateTransformer

internal fun CameraCoordinateTransformer.getTouchLockRegion(
    x: Float,
    y: Float,
    size: Int
): MeteringRectangle {
    val lockRect = toCameraSpace(RectF(x - size, y - size, x + size, y + size))

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