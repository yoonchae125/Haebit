package com.chaeyoon.haebit.obscura.model

import com.chaeyoon.haebit.obscura.ui.model.CameraValueType

data class CameraValue(
    val type: CameraValueType,
    val value: Float,
    val isFraction: Boolean = false
)