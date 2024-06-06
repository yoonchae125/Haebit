package com.chaeyoon.haebit.obscura.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chaeyoon.haebit.databinding.ViewCameraValueItemBinding
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
        val binding = ViewCameraValueItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return when (type) {
            Type.APERTURE -> ApertureViewHolder(binding)

            Type.SHUTTER_SPEED -> ShutterSpeedViewHolder(binding)

            Type.ISO -> IsoViewHolder(binding)
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
            binding.valueText.text = format(value)
        }

        private fun format(value: Float) = "ƒ${value.toOneDecimalPlaces()}"
    }

    inner class ShutterSpeedViewHolder(
        private val binding: ViewCameraValueItemBinding
    ) : CameraValueViewHolder(binding.root) {
        override fun bind(value: Float) {
            binding.valueText.text = format()
        }

        private fun format() = "${shutterSpeedStringValues[adapterPosition]}S"
    }

    inner class IsoViewHolder(
        private val binding: ViewCameraValueItemBinding
    ) : CameraValueViewHolder(binding.root) {
        override fun bind(value: Float) {
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