package com.chaeyoon.haebit.obscura.view

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.FontRes
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chaeyoon.haebit.R
import com.chaeyoon.haebit.databinding.ViewCameraValueItemBinding
import com.chaeyoon.haebit.obscura.view.model.CameraValueType
import com.chaeyoon.haebit.obscura.view.model.CameraValueUIState
import com.chaeyoon.haebit.obscura.view.model.DIFF_CALLBACK

/**
 * CameraValueListAdapter is a RecyclerView adapter designed to display a list of camera values
 * in a RecyclerView.
 */
class CameraValueListAdapter(
    private val type: CameraValueType,
    private val onClick: () -> Unit
) : ListAdapter<CameraValueUIState, CameraValueListAdapter.CameraValueViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CameraValueViewHolder {
        val binding = ViewCameraValueItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return when (type) {
            CameraValueType.APERTURE -> {
                ApertureViewHolder(binding)
            }

            CameraValueType.SHUTTER_SPEED -> {
                ShutterSpeedViewHolder(binding)
            }

            CameraValueType.ISO -> {
                IsoViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: CameraValueViewHolder, position: Int) {
        holder.bind(currentList[position])
    }


    abstract inner class CameraValueViewHolder(val binding: ViewCameraValueItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val context: Context = binding.root.context

        abstract fun bind(uiState: CameraValueUIState)

        fun Context.getTypeface(@FontRes resId: Int, path: String): Typeface =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                resources.getFont(resId)
            } else {
                Typeface.createFromAsset(resources.assets, path)
            }

        fun bindTextView(uiState: CameraValueUIState, typeface: Typeface) {
            binding.valueText.setTextColor(getTextColor(uiState))
            binding.valueText.typeface = typeface
            binding.valueText.text = uiState.value.text
            binding.root.setOnClickListener { onClick() }
        }

        private fun getTextColor(uiState: CameraValueUIState) =
            if (uiState.disabled && uiState.isSelected) {
                context.resources.getColor(R.color.selected_value, null)
            } else if (uiState.disabled) {
                context.resources.getColor(R.color.disabled_value, null)
            } else {
                context.resources.getColor(R.color.white, null)
            }
    }

    inner class ApertureViewHolder(binding: ViewCameraValueItemBinding) :
        CameraValueViewHolder(binding) {
        override fun bind(uiState: CameraValueUIState) {
            val typeface = context.getTypeface(
                R.font.reddit_mono_semi_bold,
                "reddit_mono_semi_bold.ttf"
            )
            bindTextView(uiState, typeface)
        }
    }

    inner class ShutterSpeedViewHolder(binding: ViewCameraValueItemBinding) :
        CameraValueViewHolder(binding) {
        override fun bind(uiState: CameraValueUIState) {
            val typeface = context.getTypeface(
                R.font.frank_ruhl_libre_extra_bold,
                "frank_ruhl_libre_extra_bold.ttf"
            )
            bindTextView(uiState, typeface)
            binding.valueText.setTextSize(TypedValue.COMPLEX_UNIT_SP, SHUTTER_SPEED_TEXT_SIZE)
        }
    }

    inner class IsoViewHolder(binding: ViewCameraValueItemBinding) :
        CameraValueViewHolder(binding) {
        override fun bind(uiState: CameraValueUIState) {
            val typeface = context.getTypeface(
                R.font.frank_ruhl_libre_extra_bold,
                "frank_ruhl_libre_extra_bold.ttf"
            )
            bindTextView(uiState, typeface)
        }
    }

    companion object {
        private const val SHUTTER_SPEED_TEXT_SIZE = 16f
    }
}