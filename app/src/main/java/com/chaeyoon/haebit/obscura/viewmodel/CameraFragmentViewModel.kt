package com.chaeyoon.haebit.obscura.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.chaeyoon.haebit.obscura.core.Camera
import com.chaeyoon.haebit.obscura.core.CameraImpl
import com.chaeyoon.haebit.obscura.view.AutoFitSurfaceView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class CameraFragmentViewModel(private val camera: Camera) : ViewModel() {
    val aperture: Float
        get() = camera.aperture
    val isoFlow: StateFlow<Int> = camera.isoFlow
    val shutterSpeedFlow: StateFlow<Float> = camera.shutterSpeedFlow
    val exposureValueFlow: StateFlow<Float> = camera.exposureValueFlow

    fun setCameraOutView(outView: AutoFitSurfaceView, onCameraOpenFailed: () -> Unit) {
        camera.setOutView(outView, onCameraOpenFailed)
    }

    fun startCamera(coroutineScope: CoroutineScope) {
        camera.startCamera(coroutineScope)
    }

    class Factory(private val context: Context) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CameraFragmentViewModel(CameraImpl(context)) as T
    }

}