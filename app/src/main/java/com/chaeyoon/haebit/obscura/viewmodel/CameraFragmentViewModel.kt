package com.chaeyoon.haebit.obscura.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chaeyoon.haebit.lightmeter.LightMeterCalculator
import com.chaeyoon.haebit.obscura.core.Camera
import com.chaeyoon.haebit.obscura.core.CameraImpl
import com.chaeyoon.haebit.obscura.utils.constants.apertureValues
import com.chaeyoon.haebit.obscura.utils.constants.isoValues
import com.chaeyoon.haebit.obscura.utils.constants.shutterSpeedValues
import com.chaeyoon.haebit.obscura.utils.extensions.launchAndCollect
import com.chaeyoon.haebit.obscura.utils.extensions.nearest
import com.chaeyoon.haebit.obscura.view.AutoFitSurfaceView
import com.chaeyoon.haebit.obscura.view.model.CameraValueType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CameraFragmentViewModel(
    private val camera: Camera,
    private val lightMeterCalculator: LightMeterCalculator = LightMeterCalculator()
) : ViewModel() {
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

    private val unSelectableCameraValueTextMutableFlow = MutableStateFlow(isoFlow.value.toString())
    val unSelectableCameraValueTextFlow: StateFlow<String> =
        unSelectableCameraValueTextMutableFlow.asStateFlow()

    private val unSelectableValueTypeMutableFlow = MutableStateFlow(CameraValueType.ISO)
    val unSelectableValueTypeFlow: StateFlow<CameraValueType> =
        unSelectableValueTypeMutableFlow.asStateFlow()

    init {
        viewModelScope.launch {
            exposureValueFlow.launchAndCollect(this) {
                calculateUnSelectableValue()
            }
        }

    }

    fun setCameraOutView(outView: AutoFitSurfaceView, onCameraOpenFailed: () -> Unit) {
        camera.setOutView(outView, onCameraOpenFailed)
    }

    fun startCamera(coroutineScope: CoroutineScope) {
        camera.startCamera(coroutineScope)
    }

    fun getUserCameraValueFlow(type: CameraValueType): StateFlow<Float> =
        getUserCameraValueMutableFlow(type).asStateFlow()

    fun updateUnSelectableCameraValue(value: String) {
        unSelectableCameraValueTextMutableFlow.update { value }
    }

    fun updateUserCameraValue(type: CameraValueType, value: Float) {
        getUserCameraValueMutableFlow(type).update { value }
        val unSelectableType = unSelectableValueTypeFlow.value
        if (unSelectableType != type) {
            calculateUnSelectableValue()
        }
    }

    private fun calculateUnSelectableValue() {
        val unSelectableType = unSelectableValueTypeFlow.value
        val value = when (unSelectableType) {
            CameraValueType.APERTURE -> lightMeterCalculator.calculateApertureValue(
                exposureValueFlow.value,
                userIsoMutableFlow.value,
                userShutterSpeedMutableFlow.value
            ).nearest(apertureValues)

            CameraValueType.ISO -> lightMeterCalculator.calculateIsoValue(
                exposureValueFlow.value,
                userShutterSpeedMutableFlow.value,
                userApertureMutableFlow.value
            ).nearest(isoValues)

            CameraValueType.SHUTTER_SPEED -> lightMeterCalculator.calculateShutterSpeedValue(
                exposureValueFlow.value,
                userIsoMutableFlow.value,
                userApertureMutableFlow.value
            ).nearest(shutterSpeedValues)
        }
        getUserCameraValueMutableFlow(unSelectableType).update { value }
    }

    fun onClickCameraValueList(type: CameraValueType) {
        unSelectableValueTypeMutableFlow.update { type }
    }

    private fun getUserCameraValueMutableFlow(type: CameraValueType): MutableStateFlow<Float> =
        when (type) {
            CameraValueType.APERTURE -> userApertureMutableFlow
            CameraValueType.ISO -> userIsoMutableFlow
            CameraValueType.SHUTTER_SPEED -> userShutterSpeedMutableFlow
        }

    class Factory(private val context: Context) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CameraFragmentViewModel(CameraImpl(context)) as T
    }
}