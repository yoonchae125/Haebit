package com.chaeyoon.haebit.obscura.utils.constants

import com.chaeyoon.haebit.common.extensions.toOneDecimalPlaces
import com.chaeyoon.haebit.presentation.model.CameraValueType

internal val apertureValues = listOf(
    NormalCameraValue(type = CameraValueType.APERTURE, 1f, "ƒ"),
    NormalCameraValue(type = CameraValueType.APERTURE, 1.4f, "ƒ", decimal = true),
    NormalCameraValue(type = CameraValueType.APERTURE, 2f, "ƒ"),
    NormalCameraValue(type = CameraValueType.APERTURE, 2.8f, "ƒ", decimal = true),
    NormalCameraValue(type = CameraValueType.APERTURE, 4f, "ƒ"),
    NormalCameraValue(type = CameraValueType.APERTURE, 5.6f, "ƒ", decimal = true),
    NormalCameraValue(type = CameraValueType.APERTURE, 8f, "ƒ"),
    NormalCameraValue(type = CameraValueType.APERTURE, 11f, "ƒ"),
    NormalCameraValue(type = CameraValueType.APERTURE, 16f, "ƒ"),
    NormalCameraValue(type = CameraValueType.APERTURE, 22f, "ƒ")
)

internal val shutterSpeedValues = listOf(
    FractionCameraValue(
        type = CameraValueType.SHUTTER_SPEED,
        1 / 8000f,
        "¹⁄",
        suffix = "s",
        denominator = 8000
    ),
    FractionCameraValue(
        type = CameraValueType.SHUTTER_SPEED,
        1 / 4000f,
        "¹⁄",
        suffix = "s",
        denominator = 4000
    ),
    FractionCameraValue(
        type = CameraValueType.SHUTTER_SPEED,
        1 / 2000f,
        "¹⁄",
        suffix = "s",
        denominator = 2000
    ),
    FractionCameraValue(
        type = CameraValueType.SHUTTER_SPEED,
        1 / 1000f,
        "¹⁄",
        suffix = "s",
        denominator = 1000
    ),
    FractionCameraValue(
        type = CameraValueType.SHUTTER_SPEED,
        1 / 500f,
        "¹⁄",
        suffix = "s",
        denominator = 500
    ),
    FractionCameraValue(
        type = CameraValueType.SHUTTER_SPEED,
        1 / 250f,
        "¹⁄",
        suffix = "s",
        denominator = 250
    ),
    FractionCameraValue(
        type = CameraValueType.SHUTTER_SPEED,
        1 / 125f,
        "¹⁄",
        suffix = "s",
        denominator = 125
    ),
    FractionCameraValue(
        type = CameraValueType.SHUTTER_SPEED,
        1 / 60f,
        "¹⁄",
        suffix = "s",
        denominator = 60
    ),
    FractionCameraValue(
        type = CameraValueType.SHUTTER_SPEED,
        1 / 30f,
        "¹⁄",
        suffix = "s",
        denominator = 30
    ),
    FractionCameraValue(
        type = CameraValueType.SHUTTER_SPEED,
        1 / 15f,
        "¹⁄",
        suffix = "s",
        denominator = 15
    ),
    FractionCameraValue(
        type = CameraValueType.SHUTTER_SPEED,
        1 / 8f,
        "¹⁄",
        suffix = "s",
        denominator = 8
    ),
    FractionCameraValue(
        type = CameraValueType.SHUTTER_SPEED,
        1 / 4f,
        "¹⁄",
        suffix = "s",
        denominator = 4
    ),
    FractionCameraValue(
        type = CameraValueType.SHUTTER_SPEED,
        1 / 2f,
        "¹⁄",
        suffix = "s",
        denominator = 2
    ),
    NormalCameraValue(type = CameraValueType.SHUTTER_SPEED, 1f, suffix = "s"),
    NormalCameraValue(type = CameraValueType.SHUTTER_SPEED, 2f, suffix = "s"),
    NormalCameraValue(type = CameraValueType.SHUTTER_SPEED, 4f, suffix = "s"),
    NormalCameraValue(type = CameraValueType.SHUTTER_SPEED, 8f, suffix = "s"),
    NormalCameraValue(type = CameraValueType.SHUTTER_SPEED, 16f, suffix = "s"),
    NormalCameraValue(type = CameraValueType.SHUTTER_SPEED, 30f, suffix = "s"),
    NormalCameraValue(type = CameraValueType.SHUTTER_SPEED, 60f, suffix = "s")
)

internal val isoValues = listOf(
    NormalCameraValue(type = CameraValueType.ISO, 25f),
    NormalCameraValue(type = CameraValueType.ISO, 100f),
    NormalCameraValue(type = CameraValueType.ISO, 200f),
    NormalCameraValue(type = CameraValueType.ISO, 400f),
    NormalCameraValue(type = CameraValueType.ISO, 800f),
    NormalCameraValue(type = CameraValueType.ISO, 1600f),
    NormalCameraValue(type = CameraValueType.ISO, 3200f),
    NormalCameraValue(type = CameraValueType.ISO, 12800f),
    NormalCameraValue(type = CameraValueType.ISO, 25600f),
    NormalCameraValue(type = CameraValueType.ISO, 51200f)
)

interface CameraValue {
    val type: CameraValueType
    val value: Float
    val prefix: String
    val suffix: String
    val decimal: Boolean
    fun getText(forCenter: Boolean = false): String {
        val number = if (decimal) {
            value.toOneDecimalPlaces()
        } else {
            value.toInt()
        }
        return prefix + number + suffix
    }
}

data class NormalCameraValue(
    override val type: CameraValueType,
    override val value: Float,
    override val prefix: String = "",
    override val suffix: String = "",
    override val decimal: Boolean = false
) : CameraValue

data class FractionCameraValue(
    override val type: CameraValueType,
    override val value: Float,
    override val prefix: String = "",
    override val suffix: String = "",
    override val decimal: Boolean = false,
    val denominator: Int = 1
) : CameraValue {
    override fun getText(forCenter: Boolean): String {
        val space = if (forCenter) " " else ""
        return prefix + space + denominator + suffix
    }
}