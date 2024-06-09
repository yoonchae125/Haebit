package com.chaeyoon.haebit.obscura.view

import androidx.recyclerview.widget.DiffUtil

data class CameraValueUIState(val value: Float, val isSelected: Boolean, val disabled: Boolean)

internal val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CameraValueUIState>() {
    override fun areItemsTheSame(
        oldItem: CameraValueUIState,
        newItem: CameraValueUIState
    ): Boolean =
        oldItem.value == newItem.value

    override fun areContentsTheSame(
        oldItem: CameraValueUIState,
        newItem: CameraValueUIState
    ): Boolean =
        oldItem == newItem
}
