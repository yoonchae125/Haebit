package com.chaeyoon.haebit.obscura.view

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chaeyoon.haebit.scrollview.CenterSmoothScroller
import kotlin.math.abs

/**
 * OnScrollListener to select item in the center.
 */
class CenterSelectScrollListener(
    private val recyclerView: RecyclerView,
    private val layoutManager: LinearLayoutManager,
    private val centerScroller: CenterSmoothScroller,
    private val onItemSelected: (Int) -> Unit,
) : RecyclerView.OnScrollListener() {
    private var selectedPosition = -1
    private var initialScroll = true
    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)

        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            if(!initialScroll) {
                selectCenterItem(true)
            }
            initialScroll = false
        }
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        if(!initialScroll){
            selectCenterItem()
        }
    }

    private fun selectCenterItem(scrollToCenter: Boolean = false) {
        val centerView = findCenterView(layoutManager)
        centerView?.let {
            val position = layoutManager.getPosition(it)
            if (selectedPosition != position) {
                onItemSelected(position)
                if (scrollToCenter) {
                    scrollToCenter(position)
                }
                selectedPosition = position
            }
        }
    }

    private fun findCenterView(layoutManager: LinearLayoutManager): View? {
        var minDistance = Int.MAX_VALUE
        var centerView: View? = null
        val center = recyclerView.width / 2

        for (i in 0 until layoutManager.childCount) {
            val child = layoutManager.getChildAt(i) ?: continue
            val childCenter = (child.left + child.right) / 2
            val distance = abs(childCenter - center)

            if (distance < minDistance) {
                minDistance = distance
                centerView = child
            }
        }

        return centerView
    }

    private fun scrollToCenter(position: Int) {
        centerScroller.targetPosition = position
        layoutManager.startSmoothScroll(centerScroller)
    }
}