package com.chaeyoon.haebit.obscura.ui.model

import androidx.recyclerview.widget.DiffUtil
import com.chaeyoon.haebit.obscura.utils.constants.CameraValue

data class CameraValueUIState(val value: CameraValue, val isSelected: Boolean, val disabled: Boolean)

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
