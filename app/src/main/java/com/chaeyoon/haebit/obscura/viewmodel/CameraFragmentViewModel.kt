package com.chaeyoon.haebit.obscura.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chaeyoon.haebit.obscura.core.Camera
import com.chaeyoon.haebit.obscura.core.CameraImpl
import com.chaeyoon.haebit.obscura.CameraFragment
import com.chaeyoon.haebit.obscura.core.LockState
import com.chaeyoon.haebit.obscura.utils.extensions.toTwoDecimalPlaces
import com.chaeyoon.haebit.obscura.view.model.LockRectUIState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.chaeyoon.haebit.R
import com.chaeyoon.haebit.obscura.view.AutoFitSurfaceView
import com.chaeyoon.haebit.obscura.view.model.Position

/**
 * ViewModel class for [CameraFragment].
 */
class CameraFragmentViewModel(
    private val camera: Camera
) : ViewModel() {

    // for debug
    val isoFlow = camera.isoFlow
    val exposureValueFlow = camera.exposureValueFlow
    val shutterSpeedFlow = camera.shutterSpeedFlow
    val aperture = camera.aperture
    val lensFocusDistanceFlow = camera.lensFocusDistanceFlow
    val lockStateFlow = camera.lockStateFlow

    val exposureValueTextFlow: Flow<String> = camera.exposureValueFlow.map(::toEVTextFormat)
    val lockIconVisibilityFlow: Flow<Boolean> = camera.lockStateFlow.map { it == LockState.LOCKED }
    val lockRectUIStateFlow: Flow<LockRectUIState> =
        camera.lockStateFlow.map { lockState -> LockRectUIState.from(lockState, lastTouchedPosition) }

    private var lastTouchedPosition = Position(0f, 0f)

    fun setCameraOutView(outView: AutoFitSurfaceView, onCameraOpenFailed: () -> Unit) {
        camera.setOutView(outView, onCameraOpenFailed)
    }

    fun startCamera() {
        camera.startCamera(viewModelScope)
    }

    fun lockCamera(x: Float, y: Float) {
        lastTouchedPosition = Position(x, y)
        camera.lock(x, y, viewModelScope)
    }

    fun unlockCamera() {
        camera.unLock()
    }

    private fun toEVTextFormat(value: Float): String = "EV ${value.toTwoDecimalPlaces()}"

    class Factory(private val context: Context) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CameraFragmentViewModel(CameraImpl.getInstance(context)) as T
    }
}