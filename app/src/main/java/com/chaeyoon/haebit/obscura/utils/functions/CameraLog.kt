package com.chaeyoon.haebit.obscura.utils.functions

import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureResult
import android.util.Log

fun logAEState(tag: String, result: CaptureResult) {
    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
    val stateString = when (aeState) {
        CameraMetadata.CONTROL_AE_STATE_INACTIVE -> "CONTROL_AE_STATE_INACTIVE"
        CameraMetadata.CONTROL_AE_STATE_SEARCHING -> "CONTROL_AE_STATE_SEARCHING"
        CameraMetadata.CONTROL_AE_STATE_CONVERGED -> "CONTROL_AE_STATE_CONVERGED"
        CameraMetadata.CONTROL_AE_STATE_LOCKED -> "CONTROL_AE_STATE_LOCKED"
        CameraMetadata.CONTROL_AE_STATE_FLASH_REQUIRED -> "CONTROL_AE_STATE_FLASH_REQUIRED"
        CameraMetadata.CONTROL_AE_STATE_PRECAPTURE -> "CONTROL_AE_STATE_PRECAPTURE"
        else -> "error"
    }
    Log.i(tag, "aeState $stateString")
}

fun logAFState(tag: String, result: CaptureResult) {
    val afState = result.get(CaptureResult.CONTROL_AF_STATE)
    val stateString = when (afState) {
        CameraMetadata.CONTROL_AF_STATE_INACTIVE -> "CONTROL_AF_STATE_INACTIVE"
        CameraMetadata.CONTROL_AF_STATE_PASSIVE_SCAN -> "CONTROL_AF_STATE_PASSIVE_SCAN"
        CameraMetadata.CONTROL_AF_STATE_PASSIVE_FOCUSED -> "CONTROL_AF_STATE_PASSIVE_FOCUSED"
        CameraMetadata.CONTROL_AF_STATE_ACTIVE_SCAN -> "CONTROL_AF_STATE_ACTIVE_SCAN"
        CameraMetadata.CONTROL_AF_STATE_FOCUSED_LOCKED -> "CONTROL_AF_STATE_FOCUSED_LOCKED"
        CameraMetadata.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED -> "CONTROL_AF_STATE_NOT_FOCUSED_LOCKED"
        CameraMetadata.CONTROL_AF_STATE_PASSIVE_UNFOCUSED -> "CONTROL_AF_STATE_PASSIVE_UNFOCUSED"
        else -> "error"
    }
    Log.i(tag, "afState $stateString")
}