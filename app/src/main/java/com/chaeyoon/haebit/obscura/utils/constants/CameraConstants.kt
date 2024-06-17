package com.chaeyoon.haebit.obscura.utils.constants

internal val apertureValues = listOf(
    CameraValue(1f, "ƒ1"),
    CameraValue(1.4f, "ƒ1.4"),
    CameraValue(2f, "ƒ2"),
    CameraValue(2.8f, "ƒ2.8"),
    CameraValue(4f, "ƒ4"),
    CameraValue(5.6f, "ƒ5.6"),
    CameraValue(8f, "ƒ8"),
    CameraValue(11f, "ƒ11"),
    CameraValue(16f, "ƒ16"),
    CameraValue(22f, "ƒ22")
)

internal val shutterSpeedValues = listOf(
    CameraValue(1 / 8000f, "¹⁄8000s"),
    CameraValue(1 / 4000f, "¹⁄4000s"),
    CameraValue(1 / 2000f, "¹⁄2000s"),
    CameraValue(1 / 1000f, "¹⁄1000s"),
    CameraValue(1 / 500f, "¹⁄500s"),
    CameraValue(1 / 250f, "¹⁄250s"),
    CameraValue(1 / 125f, "¹⁄125s"),
    CameraValue(1 / 60f, "¹⁄60s"),
    CameraValue(1 / 30f, "¹⁄30s"),
    CameraValue(1 / 15f, "¹⁄15s"),
    CameraValue(1 / 8f, "¹⁄8s"),
    CameraValue(1 / 4f, "¹⁄4s"),
    CameraValue(1 / 2f, "¹⁄2s"),
    CameraValue(1f, "1s"),
    CameraValue(2f, "2s"),
    CameraValue(4f, "4s"),
    CameraValue(8f, "8s"),
    CameraValue(16f, "16s"),
    CameraValue(30f, "30s"),
    CameraValue(60f, "60s")
)

internal val isoValues = listOf(
    CameraValue(25f, "25"),
    CameraValue(100f, "100"),
    CameraValue(200f, "200"),
    CameraValue(400f, "400"),
    CameraValue(800f, "800"),
    CameraValue(1600f, "1600"),
    CameraValue(3200f, "3200"),
    CameraValue(12800f, "12800"),
    CameraValue(25600f, "25600"),
    CameraValue(51200f, "51200")
)

data class CameraValue(val value: Float, val text: String)