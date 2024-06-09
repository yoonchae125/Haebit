package com.chaeyoon.haebit.obscura.view

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chaeyoon.haebit.R
import com.chaeyoon.haebit.obscura.utils.extensions.launchAndRepeatOnLifecycle
import com.chaeyoon.haebit.obscura.view.model.CameraValueType
import com.chaeyoon.haebit.obscura.view.model.CameraValueUIState
import com.chaeyoon.haebit.obscura.viewmodel.CameraFragmentViewModel
import com.chaeyoon.haebit.scrollview.CenterSmoothScroller
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@SuppressLint("ClickableViewAccessibility")
class CameraValueListBinder(
    context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val cameraValueListView: RecyclerView,
    private val valueList: List<Float>,
    private val type: CameraValueType,
    private val viewModel: CameraFragmentViewModel
) {
    private val layoutManager =
        LinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL,
            false
        )
    private val adapter = CameraValueListAdapter(type,
        { viewModel.onClickCameraValueList(type) },
        { viewModel.updateUserCameraValue(it) })
    private val userCameraValueFlow = viewModel.getUserCameraValueFlow(type)

    init {
        setDataList(type)
        setRecyclerViewPadding(context)
        setScrollListener(context)
        collectFlow()
    }

    private fun setDataList(type: CameraValueType) {
        cameraValueListView.adapter = adapter
        cameraValueListView.layoutManager = layoutManager
        adapter.submitList(getDataList())
    }

    private fun getDataList(): List<CameraValueUIState> =
        valueList.map {
            val isSelected = userCameraValueFlow.value == it
            val disabled = viewModel.unSelectableValueTypeFlow.value == type
            CameraValueUIState(
                value = it,
                isSelected = isSelected,
                disabled = disabled
            )
        }

    private fun setRecyclerViewPadding(context: Context) {
        val resources = context.resources
        val width = resources.displayMetrics.widthPixels
        val itemWidth = resources.getDimension(R.dimen.camera_value_item_width)
        val padding = ((width - itemWidth) / 2).toInt()
        cameraValueListView.setPadding(padding, 0, padding, 0)
    }

    private fun setScrollListener(context: Context) {
        cameraValueListView.addOnScrollListener(
            CenterSelectScrollListener(
                cameraValueListView,
                layoutManager,
                CenterSmoothScroller(context)
            ) { position ->
                viewModel.updateUserCameraValue(type, valueList[position])
            }
        )
    }

    private fun collectFlow() {
        lifecycleOwner.launchAndRepeatOnLifecycle {
            viewModel.unSelectableValueTypeFlow.map { it == type }
                .distinctUntilChanged()
                .collect { unSelectable ->
                    disableTouchEvent(unSelectable)
                    adapter.submitList(getDataList())
                }
        }
    }

    private fun disableTouchEvent(disabled: Boolean) {
        cameraValueListView.setOnTouchListener { _, _ ->
            disabled
        }
    }
}