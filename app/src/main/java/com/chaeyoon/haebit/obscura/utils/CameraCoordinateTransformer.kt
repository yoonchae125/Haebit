package com.chaeyoon.haebit.obscura.utils

import android.graphics.Matrix
import android.graphics.RectF
import android.hardware.camera2.CameraCharacteristics


/**
 * Transform coordinates to and from preview coordinate space and camera driver
 * coordinate space.
 */
class CameraCoordinateTransformer(chr: CameraCharacteristics, previewRect: RectF) {
    private val mPreviewToCameraTransform: Matrix
    private val mDriverRectF: RectF

    init {
        require(hasNonZeroArea(previewRect)) { "previewRect" }
        val rect = chr.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)
        val sensorOrientation = chr.get(CameraCharacteristics.SENSOR_ORIENTATION)
        val rotation = sensorOrientation ?: 90
        mDriverRectF = RectF(rect)
        val face = chr.get(CameraCharacteristics.LENS_FACING)
        val mirrorX = face != null && face == CameraCharacteristics.LENS_FACING_FRONT
        mPreviewToCameraTransform = previewToCameraTransform(mirrorX, rotation, previewRect)
    }

    /**
     * Transform a rectangle in preview view space into a new rectangle in
     * camera view space.
     * @param source the rectangle in preview view space
     * @return the rectangle in camera view space.
     */
    fun toCameraSpace(source: RectF?): RectF {
        val result = RectF()
        mPreviewToCameraTransform.mapRect(result, source)
        return result
    }

    private fun previewToCameraTransform(
        mirrorX: Boolean, sensorOrientation: Int,
        previewRect: RectF
    ): Matrix {
        val transform = Matrix()
        // Need mirror for front camera.
        transform.setScale((if (mirrorX) -1 else 1).toFloat(), 1f)
        // Because preview orientation is different  form sensor orientation,
        // rotate to same orientation, Counterclockwise.
        transform.postRotate(-sensorOrientation.toFloat())
        // Map rotated matrix to preview rect
        transform.mapRect(previewRect)
        // Map  preview coordinates to driver coordinates
        val fill = Matrix()
        fill.setRectToRect(previewRect, mDriverRectF, Matrix.ScaleToFit.FILL)
        // Concat the previous transform on top of the fill behavior.
        transform.setConcat(fill, transform)
        // finally get transform matrix
        return transform
    }

    private fun hasNonZeroArea(rect: RectF): Boolean {
        return rect.width() != 0f && rect.height() != 0f
    }
}