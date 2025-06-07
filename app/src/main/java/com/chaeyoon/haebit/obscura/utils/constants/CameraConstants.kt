package com.chaeyoon.haebit.obscura.utils.constants

import com.chaeyoon.haebit.obscura.model.CameraValue
import com.chaeyoon.haebit.obscura.ui.model.CameraValueType

//"¹⁄"
//"ƒ"
internal val initialApertureValues = listOf(
    CameraValue(
        type = CameraValueType.APERTURE,
        value = 1f
    ),
    CameraValue(
        type = CameraValueType.APERTURE,
        value = 1.4f
    ),
    CameraValue(
        type = CameraValueType.APERTURE,
        value = 2f
    ),
    CameraValue(
        type = CameraValueType.APERTURE,
        value = 2.8f
    ),
    CameraValue(
        type = CameraValueType.APERTURE,
        value = 4f
    ),
    CameraValue(
        type = CameraValueType.APERTURE,
        value = 5.6f
    ),
    CameraValue(
        type = CameraValueType.APERTURE,
        value = 8f
    ),
    CameraValue(
        type = CameraValueType.APERTURE,
        value = 11f
    ),
    CameraValue(
        type = CameraValueType.APERTURE,
        value = 16f
    ),
    CameraValue(
        type = CameraValueType.APERTURE,
        value = 22f
    )
)

internal val initialShutterSpeedValues = listOf(
    CameraValue(
        type = CameraValueType.SHUTTER_SPEED,
        value = 8000f,
        isFraction = true
    ),
    CameraValue(
        type = CameraValueType.SHUTTER_SPEED,
        value = 4000f,
        isFraction = true
    ),
    CameraValue(
        type = CameraValueType.SHUTTER_SPEED,
        value = 2000f,
        isFraction = true

    ),
    CameraValue(
        type = CameraValueType.SHUTTER_SPEED,
        value = 1000f,
        isFraction = true
    ),
    CameraValue(
        type = CameraValueType.SHUTTER_SPEED,
        value = 500f,
        isFraction = true

    ),
    CameraValue(
        type = CameraValueType.SHUTTER_SPEED,
        value = 250f,
        isFraction = true
    ),
    CameraValue(
        type = CameraValueType.SHUTTER_SPEED,
        value = 125f,
        isFraction = true

    ),
    CameraValue(
        type = CameraValueType.SHUTTER_SPEED,
        value = 60f,
        isFraction = true
    ),
    CameraValue(
        type = CameraValueType.SHUTTER_SPEED,
        value = 30f,
        isFraction = true
    ),
    CameraValue(
        type = CameraValueType.SHUTTER_SPEED,
        value = 15f,
        isFraction = true
    ),
    CameraValue(
        type = CameraValueType.SHUTTER_SPEED,
        value = 8f,
        isFraction = true
    ),
    CameraValue(
        type = CameraValueType.SHUTTER_SPEED,
        value = 4f,
        isFraction = true
    ),
    CameraValue(
        type = CameraValueType.SHUTTER_SPEED,
        value = 2f,
        isFraction = true
    ),
    CameraValue(
        type = CameraValueType.SHUTTER_SPEED,
        value = 1f
    ),
    CameraValue(
        type = CameraValueType.SHUTTER_SPEED,
        value = 2f
    ),
    CameraValue(
        type = CameraValueType.SHUTTER_SPEED,
        value = 4f
    ),
    CameraValue(
        type = CameraValueType.SHUTTER_SPEED,
        value = 8f
    ),
    CameraValue(
        type = CameraValueType.SHUTTER_SPEED,
        value = 16f
    ),
    CameraValue(
        type = CameraValueType.SHUTTER_SPEED,
        value = 30f
    ),
    CameraValue(type = CameraValueType.SHUTTER_SPEED,
        value = 60f
    )
)

internal val initialIsoValues = listOf(
    CameraValue(type = CameraValueType.ISO, value = 25f),
    CameraValue(type = CameraValueType.ISO, value = 100f),
    CameraValue(type = CameraValueType.ISO, 200f),
    CameraValue(type = CameraValueType.ISO, value = 400f),
    CameraValue(type = CameraValueType.ISO, value = 800f),
    CameraValue(type = CameraValueType.ISO, value = 1600f),
    CameraValue(type = CameraValueType.ISO, value = 3200f),
    CameraValue(type = CameraValueType.ISO, value = 12800f),
    CameraValue(type = CameraValueType.ISO, value = 25600f),
    CameraValue(type = CameraValueType.ISO, value = 51200f)
)