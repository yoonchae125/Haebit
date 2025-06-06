package com.chaeyoon.haebit.obscura.ui.model.converter

import com.chaeyoon.haebit.obscura.model.CameraValue
import com.chaeyoon.haebit.obscura.ui.model.CameraValueType
import com.chaeyoon.haebit.obscura.ui.model.CameraValueUIState
import com.chaeyoon.haebit.obscura.utils.extensions.hasDecimalPart
import com.chaeyoon.haebit.obscura.utils.extensions.toOneDecimalPlaces

internal fun CameraValue.toUIState(
    isSelected: Boolean = false,
    disabled: Boolean = false
) = CameraValueUIState(
    type = type,
    value = if (isFraction) {
        1 / value
    } else {
        value
    },
    text = getText(),
    isSelected = isSelected,
    disabled = disabled
)

internal fun CameraValue.getText(): String = when (type) {
    CameraValueType.APERTURE -> if (value.hasDecimalPart()) {
        "ƒ${value.toOneDecimalPlaces()}"
    } else {
        "ƒ${value.toInt()}"
    }

    CameraValueType.SHUTTER_SPEED -> {
        if (isFraction) {
            "¹⁄${value.toInt()}s"
        } else {
            "${value.toInt()}s"
        }
    }

    CameraValueType.ISO -> value.toInt().toString()
}