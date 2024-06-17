package com.chaeyoon.haebit.obscura.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.chaeyoon.haebit.obscura.core.Camera
import com.chaeyoon.haebit.obscura.core.CameraImpl
import com.chaeyoon.haebit.obscura.CameraFragment
import com.chaeyoon.haebit.obscura.utils.extensions.toTwoDecimalPlaces
import com.chaeyoon.haebit.obscura.view.AutoFitSurfaceView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * ViewModel class for [CameraFragment].
 */
class CameraFragmentViewModel(
    private val camera: Camera
) : ViewModel() {
    val exposureValueTextFlow: Flow<String> = camera.exposureValueFlow.map { it.toEVTextFormat() }
    private fun Float.toEVTextFormat(): String = "EV ${toTwoDecimalPlaces()}"
    fun setCameraOutView(outView: AutoFitSurfaceView, onCameraOpenFailed: () -> Unit) {
        camera.setOutView(outView, onCameraOpenFailed)
    }

    fun startCamera(coroutineScope: CoroutineScope) {
        camera.startCamera(coroutineScope)
    }

    class Factory(private val context: Context) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CameraFragmentViewModel(CameraImpl.getInstance(context)) as T
    }
}