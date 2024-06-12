package com.chaeyoon.haebit.obscura.view

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chaeyoon.haebit.R
import com.chaeyoon.haebit.obscura.utils.extensions.launchAndCollect
import com.chaeyoon.haebit.obscura.utils.extensions.launchAndRepeatOnLifecycle
import com.chaeyoon.haebit.obscura.view.model.CameraValueType
import com.chaeyoon.haebit.obscura.view.model.CameraValueUIState
import com.chaeyoon.haebit.obscura.viewmodel.CameraFragmentViewModel
import com.chaeyoon.haebit.scrollview.CenterSmoothScroller
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Binder to manage camera value list.
 */
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
        { viewModel.updateUnSelectableCameraValue(it) })
    private val centerScroller = CenterSmoothScroller(context)
    private val userCameraValueFlow = viewModel.getUserCameraValueFlow(type)

    init {
        initRecyclerView()
        setRecyclerViewPadding(context)
        setScrollListener()
        collectFlow()
    }

    private fun initRecyclerView() {
        cameraValueListView.adapter = adapter
        cameraValueListView.layoutManager = layoutManager
        cameraValueListView.itemAnimator = null
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

    private fun setScrollListener() {
        var selectedPosition = -1
        cameraValueListView.addOnScrollListener(
            CenterSelectScrollListener(
                cameraValueListView,
                layoutManager,
                centerScroller
            ) { position ->
                if (selectedPosition != position) {
                    viewModel.updateUserCameraValue(type, valueList[position])
                    selectedPosition = position
                }
            }
        )
    }

    private fun collectFlow() {
        lifecycleOwner.launchAndRepeatOnLifecycle {
            viewModel.unSelectableValueTypeFlow.map { it == type }
                .distinctUntilChanged()
                .launchAndCollect(this) { unSelectable ->
                    disableTouchEvent(unSelectable)
                    adapter.submitList(getDataList())
                }

            userCameraValueFlow.launchAndCollect(this) {
                adapter.submitList(getDataList())
                scrollToCenter(it)
            }
        }
    }

    private fun scrollToCenter(value: Float) {
        val target = valueList.indexOf(value)
        if (target < 0) return
        if (centerScroller.targetPosition == target) return
        centerScroller.targetPosition = target
        layoutManager.startSmoothScroll(centerScroller)
    }

    private fun disableTouchEvent(disabled: Boolean) {
        cameraValueListView.setOnTouchListener { _, _ ->
            disabled
        }
    }
}