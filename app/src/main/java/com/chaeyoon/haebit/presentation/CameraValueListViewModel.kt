package com.chaeyoon.haebit.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chaeyoon.haebit.obscura.core.Camera
import com.chaeyoon.haebit.obscura.core.CameraImpl
import com.chaeyoon.haebit.obscura.utils.constants.CameraValue
import com.chaeyoon.haebit.obscura.utils.constants.apertureValues
import com.chaeyoon.haebit.obscura.utils.constants.isoValues
import com.chaeyoon.haebit.obscura.utils.constants.shutterSpeedValues
import com.chaeyoon.haebit.ui.extensions.launchAndCollect
import com.chaeyoon.haebit.common.extensions.nearest
import com.chaeyoon.haebit.ui.valuelist.CameraValueListBinder
import com.chaeyoon.haebit.presentation.model.CameraValueType
import com.chaeyoon.lightmeter.LightMeterCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel class for [CameraValueListBinder].
 */
class CameraValueListViewModel(
    private val camera: Camera,
    private val lightMeterCalculator: LightMeterCalculator = LightMeterCalculator()
) : ViewModel() {
    private val aperture: Float
        get() = camera.aperture
    private val isoFlow: StateFlow<Float> = camera.isoFlow
    private val shutterSpeedFlow: StateFlow<Float> = camera.shutterSpeedFlow
    private val exposureValueFlow: StateFlow<Float> = camera.exposureValueFlow

    private val mutableUserIsoFlow = MutableStateFlow(
        isoFlow.value.nearest(isoValues)
    )
    private val mutableUserShutterSpeedFlow = MutableStateFlow(
        shutterSpeedFlow.value.nearest(shutterSpeedValues)
    )
    private val mutableUserApertureFlow = MutableStateFlow(
        aperture.nearest(apertureValues)
    )

    private val mutableUnSelectableCameraValueTextFlow = MutableStateFlow(isoFlow.value.nearest(isoValues))
    val unSelectableCameraValueTextFlow: StateFlow<CameraValue> =
        mutableUnSelectableCameraValueTextFlow.asStateFlow()

    private val mutableUnSelectableValueTypeFlow = MutableStateFlow(CameraValueType.ISO)
    val unSelectableValueTypeFlow: StateFlow<CameraValueType> =
        mutableUnSelectableValueTypeFlow.asStateFlow()

    init {
        viewModelScope.launch {
            exposureValueFlow.launchAndCollect(this) {
                calculateUnSelectableValue()
            }
        }
    }

    fun getUserCameraValueFlow(type: CameraValueType): StateFlow<CameraValue> =
        getUserCameraValueMutableFlow(type).asStateFlow()

    private fun updateUnSelectableCameraValue(value: CameraValue) {
        val unSelectableType = unSelectableValueTypeFlow.value
        getUserCameraValueMutableFlow(unSelectableType).update { value }
        mutableUnSelectableCameraValueTextFlow.update { value }
    }

    fun updateUserCameraValue(type: CameraValueType, value: CameraValue) {
        if (type == unSelectableValueTypeFlow.value) return
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
                mutableUserIsoFlow.value.value,
                mutableUserShutterSpeedFlow.value.value
            ).nearest(apertureValues)

            CameraValueType.ISO -> lightMeterCalculator.calculateIsoValue(
                exposureValueFlow.value,
                mutableUserShutterSpeedFlow.value.value,
                mutableUserApertureFlow.value.value
            ).nearest(isoValues)

            CameraValueType.SHUTTER_SPEED -> lightMeterCalculator.calculateShutterSpeedValue(
                exposureValueFlow.value,
                mutableUserIsoFlow.value.value,
                mutableUserApertureFlow.value.value
            ).nearest(shutterSpeedValues)
        }
        updateUnSelectableCameraValue(value)
        getUserCameraValueMutableFlow(unSelectableType).update { value }
    }


    fun onClickCameraValueList(type: CameraValueType) {
        mutableUnSelectableValueTypeFlow.update { type }

        val value = getUserCameraValueMutableFlow(type).value
        updateUnSelectableCameraValue(value)
    }

    private fun getUserCameraValueMutableFlow(type: CameraValueType): MutableStateFlow<CameraValue> =
        when (type) {
            CameraValueType.APERTURE -> mutableUserApertureFlow
            CameraValueType.ISO -> mutableUserIsoFlow
            CameraValueType.SHUTTER_SPEED -> mutableUserShutterSpeedFlow
        }

    class Factory(
        private val context: Context,
        private val coroutineScope: CoroutineScope
    ) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CameraValueListViewModel(CameraImpl.getInstance(context, coroutineScope)) as T
    }
}