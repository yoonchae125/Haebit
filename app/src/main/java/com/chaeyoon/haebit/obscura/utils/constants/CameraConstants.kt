package com.chaeyoon.haebit.obscura.utils.constants

import com.chaeyoon.haebit.obscura.utils.extensions.toOneDecimalPlaces

internal val apertureValues = listOf(
    NormalCameraValue(1f, "ƒ"),
    NormalCameraValue(1.4f, "ƒ", decimal = true),
    NormalCameraValue(2f, "ƒ"),
    NormalCameraValue(2.8f, "ƒ", decimal = true),
    NormalCameraValue(4f, "ƒ"),
    NormalCameraValue(5.6f, "ƒ", decimal = true),
    NormalCameraValue(8f, "ƒ"),
    NormalCameraValue(11f, "ƒ"),
    NormalCameraValue(16f, "ƒ"),
    NormalCameraValue(22f, "ƒ")
)

internal val shutterSpeedValues = listOf(
    FractionCameraValue(1 / 8000f, "¹⁄", suffix = "s", denominator = 8000),
    FractionCameraValue(1 / 4000f, "¹⁄", suffix = "s", denominator = 4000),
    FractionCameraValue(1 / 2000f, "¹⁄", suffix = "s", denominator = 2000),
    FractionCameraValue(1 / 1000f, "¹⁄", suffix = "s", denominator = 1000),
    FractionCameraValue(1 / 500f, "¹⁄", suffix = "s", denominator = 500),
    FractionCameraValue(1 / 250f, "¹⁄", suffix = "s", denominator = 250),
    FractionCameraValue(1 / 125f, "¹⁄", suffix = "s", denominator = 125),
    FractionCameraValue(1 / 60f, "¹⁄", suffix = "s", denominator = 60),
    FractionCameraValue(1 / 30f, "¹⁄", suffix = "s", denominator = 30),
    FractionCameraValue(1 / 15f, "¹⁄", suffix = "s", denominator = 15),
    FractionCameraValue(1 / 8f, "¹⁄", suffix = "s", denominator = 8),
    FractionCameraValue(1 / 4f, "¹⁄", suffix = "s", denominator = 4),
    FractionCameraValue(1 / 2f, "¹⁄", suffix = "s", denominator = 2),
    NormalCameraValue(1f, suffix = "s"),
    NormalCameraValue(2f, suffix = "s"),
    NormalCameraValue(4f, suffix = "s"),
    NormalCameraValue(8f, suffix = "s"),
    NormalCameraValue(16f, suffix = "s"),
    NormalCameraValue(30f, suffix = "s"),
    NormalCameraValue(60f, suffix = "s")
)

internal val isoValues = listOf(
    NormalCameraValue(25f),
    NormalCameraValue(100f),
    NormalCameraValue(200f),
    NormalCameraValue(400f),
    NormalCameraValue(800f),
    NormalCameraValue(1600f),
    NormalCameraValue(3200f),
    NormalCameraValue(12800f),
    NormalCameraValue(25600f),
    NormalCameraValue(51200f)
)

interface CameraValue {
    val value: Float
    val prefix: String
    val suffix: String
    val decimal: Boolean
    fun getText(): String {
        val number = if (decimal) {
            value.toOneDecimalPlaces()
        } else {
            value.toInt()
        }
        return prefix + number + suffix
    }
}

data class NormalCameraValue(
    override val value: Float,
    override val prefix: String = "",
    override val suffix: String = "",
    override val decimal: Boolean = false
) : CameraValue

data class FractionCameraValue(
    override val value: Float,
    override val prefix: String = "",
    override val suffix: String = "",
    override val decimal: Boolean = false,
    val denominator: Int = 1
) : CameraValue {
    override fun getText(): String {
        return prefix + denominator + suffix
    }
}