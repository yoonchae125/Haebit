package com.chaeyoon.haebit.obscura.view

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.FontRes
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chaeyoon.haebit.R
import com.chaeyoon.haebit.databinding.ViewCameraValueItemBinding
import com.chaeyoon.haebit.databinding.ViewShutterSpeedItemBinding
import com.chaeyoon.haebit.obscura.utils.constants.shutterSpeedStringValues
import com.chaeyoon.haebit.obscura.utils.extensions.toOneDecimalPlaces
import com.chaeyoon.haebit.obscura.view.model.CameraValueType
import com.chaeyoon.haebit.obscura.view.model.CameraValueUIState
import com.chaeyoon.haebit.obscura.view.model.DIFF_CALLBACK

/**
 * CameraValueListAdapter is a RecyclerView adapter designed to display a list of camera values
 * in a RecyclerView.
 */
class CameraValueListAdapter(
    private val type: CameraValueType,
    private val onClick: () -> Unit,
    private val updateCenterValue: (String) -> Unit
) : ListAdapter<CameraValueUIState, CameraValueListAdapter.CameraValueViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CameraValueViewHolder {

        return when (type) {
            CameraValueType.APERTURE -> {
                val binding = ViewCameraValueItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ApertureViewHolder(binding)
            }

            CameraValueType.SHUTTER_SPEED -> {
                val binding = ViewShutterSpeedItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ShutterSpeedViewHolder(binding)
            }

            CameraValueType.ISO -> {
                val binding = ViewCameraValueItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                IsoViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: CameraValueViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    abstract inner class CameraValueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(uiState: CameraValueUIState)
    }

    private fun getTextColor(context: Context, uiState: CameraValueUIState) =
        if (uiState.disabled && uiState.isSelected) {
            context.resources.getColor(R.color.selected_value, null)
        } else if (uiState.disabled) {
            context.resources.getColor(R.color.disabled_value, null)
        } else {
            context.resources.getColor(R.color.white, null)
        }

    private fun Context.getTypeface(@FontRes resId: Int, path: String) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            resources.getFont(resId)
        } else {
            Typeface.createFromAsset(resources.assets, path)
        }

    inner class ApertureViewHolder(
        private val binding: ViewCameraValueItemBinding
    ) : CameraValueViewHolder(binding.root) {
        override fun bind(uiState: CameraValueUIState) {
            val context = binding.root.context
            val typeface = context.getTypeface(
                R.font.reddit_mono_semi_bold,
                "reddit_mono_semi_bold.ttf"
            )
            binding.valueText.typeface = typeface
            binding.valueText.text = format(uiState.value)
            binding.root.setOnClickListener { onClick() }
            binding.valueText.setTextColor(getTextColor(context, uiState))
            if (uiState.isSelected && uiState.disabled) {
                updateCenterValue(format(uiState.value))
            }
        }

        private fun format(value: Float) = "ƒ${value.toOneDecimalPlaces()}"
    }

    inner class ShutterSpeedViewHolder(
        private val binding: ViewShutterSpeedItemBinding
    ) : CameraValueViewHolder(binding.root) {
        override fun bind(uiState: CameraValueUIState) {
            val context = binding.root.context
            val typeface = context.getTypeface(
                R.font.frank_ruhl_libre_extra_bold,
                "frank_ruhl_libre_extra_bold.ttf"
            )
            binding.valueText.typeface = typeface
            binding.valueText.text = shutterSpeedStringValues[adapterPosition]
            binding.root.setOnClickListener { onClick() }
            binding.valueText.setTextColor(getTextColor(context, uiState))
            if (uiState.isSelected && uiState.disabled) {
                updateCenterValue(shutterSpeedStringValues[adapterPosition])
            }
        }
    }

    inner class IsoViewHolder(
        private val binding: ViewCameraValueItemBinding
    ) : CameraValueViewHolder(binding.root) {
        override fun bind(uiState: CameraValueUIState) {
            val context = binding.root.context
            val typeface = context.getTypeface(
                R.font.frank_ruhl_libre_extra_bold,
                "frank_ruhl_libre_extra_bold.ttf"
            )
            binding.valueText.typeface = typeface
            binding.valueText.text = format(uiState.value)
            binding.root.setOnClickListener { onClick() }
            binding.valueText.setTextColor(getTextColor(context, uiState))
            if (uiState.isSelected && uiState.disabled) {
                updateCenterValue(format(uiState.value))
            }
        }

        private fun format(value: Float) = value.toInt().toString()
    }
}