package com.chaeyoon.haebit.scrollview

import android.content.Context
import android.util.DisplayMetrics
import androidx.recyclerview.widget.LinearSmoothScroller

/**
 * CenterSmoothScroller is a custom implementation of LinearSmoothScroller
 * designed to smoothly scroll a RecyclerView item to the center of the viewport.
 *
 * This class overrides the calculateDtToFit method to compute the offset needed
 * to position the target view at the center of the RecyclerView's visible area.
 */
class CenterSmoothScroller(context: Context) : LinearSmoothScroller(context) {
    override fun calculateDtToFit(
        viewStart: Int,
        viewEnd: Int,
        boxStart: Int,
        boxEnd: Int,
        snapPreference: Int
    ): Int {
        return (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2)
    }

    override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
        return MILLISECONDS_PER_INCH / displayMetrics.densityDpi
    }

    companion object {
        private const val MILLISECONDS_PER_INCH = 200f
    }
}