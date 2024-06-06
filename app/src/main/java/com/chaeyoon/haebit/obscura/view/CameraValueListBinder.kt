package com.chaeyoon.haebit.obscura.view

import android.content.Context
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chaeyoon.haebit.R
import com.chaeyoon.haebit.scrollview.CenterSmoothScroller

class CameraValueListBinder(
    context: Context,
    private val cameraValueList: RecyclerView,
    private val dataList: List<Float>,
    type: CameraValueListAdapter.Type
) {
    private val layoutManager =
        LinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL,
            false
        )

    init {
        setDataList(type)
        setRecyclerViewPadding(context)
        setScrollListener(context)
    }

    private fun setDataList(type: CameraValueListAdapter.Type) {
        val adapter = CameraValueListAdapter(type)
        cameraValueList.adapter = adapter
        cameraValueList.layoutManager = layoutManager
        adapter.submitList(dataList)
    }


    private fun setRecyclerViewPadding(context: Context) {
        val resources = context.resources
        val width = resources.displayMetrics.widthPixels
        val itemWidth = resources.getDimension(R.dimen.camera_value_item_width)
        val padding = ((width - itemWidth) / 2).toInt()
        cameraValueList.setPadding(padding, 0, padding, 0)
    }

    private fun setScrollListener(context: Context) {
        var selectedPosition = 0
        cameraValueList.addOnScrollListener(
            CenterSelectScrollListener(
                cameraValueList,
                layoutManager,
                CenterSmoothScroller(context)
            ) { position ->
                if (position != selectedPosition) {
                    // TODO: Handle item selection
                    Log.d("SelectedItem", "Selected item position: $position")
                    selectedPosition = position
                }
            }
        )
    }
}