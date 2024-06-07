package com.chaeyoon.haebit.obscura.view

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.FontRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chaeyoon.haebit.R
import com.chaeyoon.haebit.databinding.ViewCameraValueItemBinding
import com.chaeyoon.haebit.databinding.ViewShutterSpeedItemBinding
import com.chaeyoon.haebit.obscura.utils.constants.shutterSpeedStringValues
import com.chaeyoon.haebit.obscura.utils.extensions.toOneDecimalPlaces

/**
 * CameraValueListAdapter is a RecyclerView adapter designed to display a list of camera values
 * in a RecyclerView.
 */
class CameraValueListAdapter(private val type: Type) :
    ListAdapter<Float, CameraValueListAdapter.CameraValueViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CameraValueViewHolder {

        return when (type) {
            Type.APERTURE -> {
                val binding = ViewCameraValueItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ApertureViewHolder(binding)
            }

            Type.SHUTTER_SPEED -> {
                val binding = ViewShutterSpeedItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ShutterSpeedViewHolder(binding)
            }

            Type.ISO -> {
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
        abstract fun bind(value: Float)
    }

    inner class ApertureViewHolder(
        private val binding: ViewCameraValueItemBinding
    ) : CameraValueViewHolder(binding.root) {
        override fun bind(value: Float) {
            val typeface =
                binding.root.context.getTypeface(
                    R.font.reddit_mono_semi_bold,
                    "reddit_mono_semi_bold.ttf"
                )
            binding.valueText.typeface = typeface
            binding.valueText.text = format(value)
        }

        private fun format(value: Float) = "ƒ${value.toOneDecimalPlaces()}"
    }

    private fun Context.getTypeface(@FontRes resId: Int, path: String) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            resources.getFont(resId)
        } else {
            Typeface.createFromAsset(resources.assets, path)
        }

    inner class ShutterSpeedViewHolder(
        private val binding: ViewShutterSpeedItemBinding
    ) : CameraValueViewHolder(binding.root) {
        override fun bind(value: Float) {
            val typeface =
                binding.root.context.getTypeface(
                    R.font.frank_ruhl_libre_extra_bold,
                    "frank_ruhl_libre_extra_bold.ttf"
                )
            binding.valueText.typeface = typeface
            binding.valueText.text = shutterSpeedStringValues[adapterPosition]
        }
    }

    inner class IsoViewHolder(
        private val binding: ViewCameraValueItemBinding
    ) : CameraValueViewHolder(binding.root) {
        override fun bind(value: Float) {
            val typeface =
                binding.root.context.getTypeface(
                    R.font.frank_ruhl_libre_extra_bold,
                    "frank_ruhl_libre_extra_bold.ttf"
                )
            binding.valueText.typeface = typeface
            binding.valueText.text = format(value)
        }

        private fun format(value: Float) = value.toInt().toString()
    }

    enum class Type {
        APERTURE, SHUTTER_SPEED, ISO
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Float>() {
            override fun areItemsTheSame(oldItem: Float, newItem: Float): Boolean =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: Float, newItem: Float): Boolean =
                oldItem == newItem
        }
    }
}