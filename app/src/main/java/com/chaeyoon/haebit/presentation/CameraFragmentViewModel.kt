package com.chaeyoon.haebit.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.chaeyoon.haebit.obscura.core.Camera
import com.chaeyoon.haebit.obscura.core.CameraImpl
import com.chaeyoon.haebit.ui.CameraFragment
import com.chaeyoon.haebit.obscura.core.LockState
import com.chaeyoon.haebit.common.extensions.toTwoDecimalPlaces
import com.chaeyoon.haebit.presentation.model.LockRectUIState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.chaeyoon.haebit.ui.view.AutoFitSurfaceView
import com.chaeyoon.haebit.common.model.Position
import kotlinx.coroutines.CoroutineScope

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
    val vibrateFlow = camera.vibrateFlow

    val exposureValueTextFlow: Flow<String> = camera.exposureValueFlow.map(::toEVTextFormat)
    val lockIconVisibilityFlow: Flow<Boolean> = camera.lockStateFlow.map { it == LockState.LOCKED }
    val lockRectUIStateFlow: Flow<LockRectUIState> =
        camera.lockStateFlow.map { lockState ->
            LockRectUIState.from(
                lockState,
                lastTouchedPosition
            )
        }


    private var lastTouchedPosition = Position(0f, 0f)

    fun setCameraOutView(outView: AutoFitSurfaceView, onCameraOpenFailed: () -> Unit) {
        camera.setOutView(outView, onCameraOpenFailed)
    }

    fun startCamera() {
        camera.startCamera()
    }

    fun closeCamera() {
        camera.closeCamera()
    }

    fun lockCamera(x: Float, y: Float) {
        lastTouchedPosition = Position(x, y)
        camera.lock(x, y)
    }

    fun unlockCamera() {
        camera.unLock()
    }

    private fun toEVTextFormat(value: Float): String = "EV ${value.toTwoDecimalPlaces()}"

    class Factory(
        private val context: Context,
        private val coroutineScope: CoroutineScope
    ) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CameraFragmentViewModel(CameraImpl.getInstance(context, coroutineScope)) as T
    }
}