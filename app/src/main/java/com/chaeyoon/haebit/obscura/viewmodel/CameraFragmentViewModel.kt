package com.chaeyoon.haebit.obscura.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.chaeyoon.haebit.obscura.core.Camera
import com.chaeyoon.haebit.obscura.core.CameraImpl
import com.chaeyoon.haebit.obscura.utils.constants.apertureValues
import com.chaeyoon.haebit.obscura.utils.constants.isoValues
import com.chaeyoon.haebit.obscura.utils.constants.shutterSpeedValues
import com.chaeyoon.haebit.obscura.utils.extensions.nearest
import com.chaeyoon.haebit.obscura.view.AutoFitSurfaceView
import com.chaeyoon.haebit.obscura.view.model.CameraValueType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CameraFragmentViewModel(private val camera: Camera) : ViewModel() {
    val aperture: Float
        get() = camera.aperture
    val isoFlow: StateFlow<Float> = camera.isoFlow
    val shutterSpeedFlow: StateFlow<Float> = camera.shutterSpeedFlow
    val exposureValueFlow: StateFlow<Float> = camera.exposureValueFlow

    private val userIsoMutableFlow = MutableStateFlow(
        camera.isoFlow.value.nearest(isoValues)
    )
    private val userShutterSpeedMutableFlow = MutableStateFlow(
        camera.shutterSpeedFlow.value.nearest(shutterSpeedValues)
    )
    private val userApertureMutableFlow = MutableStateFlow(
        camera.aperture.nearest(apertureValues)
    )
    val userIsoFlow: StateFlow<Float> = userIsoMutableFlow.asStateFlow()
    val userShutterSpeedFlow: StateFlow<Float> = userShutterSpeedMutableFlow.asStateFlow()
    val userApertureFlow: StateFlow<Float> = userApertureMutableFlow.asStateFlow()


    private val cameraValueTextMutableFlow = MutableStateFlow(isoFlow.value.toString())
    val cameraValueTextFlow: StateFlow<String> = cameraValueTextMutableFlow.asStateFlow()

    private val unSelectableValueTypeMutableFlow = MutableStateFlow(CameraValueType.ISO)
    val unSelectableValueTypeFlow: StateFlow<CameraValueType> =
        unSelectableValueTypeMutableFlow.asStateFlow()

    fun setCameraOutView(outView: AutoFitSurfaceView, onCameraOpenFailed: () -> Unit) {
        camera.setOutView(outView, onCameraOpenFailed)
    }

    fun startCamera(coroutineScope: CoroutineScope) {
        camera.startCamera(coroutineScope)
    }

    fun getUserCameraValueFlow(type: CameraValueType): StateFlow<Float> =
        when (type) {
            CameraValueType.APERTURE -> userApertureFlow
            CameraValueType.ISO -> userIsoFlow
            CameraValueType.SHUTTER_SPEED -> userShutterSpeedFlow
        }

    fun updateUserCameraValue(value: String) {
        cameraValueTextMutableFlow.update { value }
    }

    fun updateUserCameraValue(type: CameraValueType, value: Float) {
        when (type) {
            CameraValueType.APERTURE -> userApertureMutableFlow
            CameraValueType.ISO -> userIsoMutableFlow
            CameraValueType.SHUTTER_SPEED -> userShutterSpeedMutableFlow
        }.update { value }
    }

    fun onClickCameraValueList(type: CameraValueType) {
        unSelectableValueTypeMutableFlow.update { type }
    }

    class Factory(private val context: Context) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CameraFragmentViewModel(CameraImpl(context)) as T
    }
}