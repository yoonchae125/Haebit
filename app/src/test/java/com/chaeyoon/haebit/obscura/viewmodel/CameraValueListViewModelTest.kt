package com.chaeyoon.haebit.obscura.viewmodel

import com.chaeyoon.haebit.CoroutineDispatcherTestRule
import com.chaeyoon.haebit.obscura.core.Camera
import com.chaeyoon.haebit.obscura.view.model.CameraValueType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals


@RunWith(RobolectricTestRunner::class)
class CameraValueListViewModelTest {
    @ExperimentalCoroutinesApi
    @get:Rule
    val mainDispatcherRule = CoroutineDispatcherTestRule()

    private val camera: Camera = mock {
        on { it.exposureValueFlow } doReturn MutableStateFlow(8.79f)
        on { it.aperture } doReturn 16f
        on { it.shutterSpeedFlow } doReturn MutableStateFlow(1 / 8000f)
        on { it.isoFlow } doReturn MutableStateFlow(0f)
    }

    private val testInstance = CameraValueListViewModel(camera)

    @Test
    fun test() {
        testInstance.onClickCameraValueList(CameraValueType.ISO)
        // ev 8.79
        // f16
        // 1/8000s
        // 51200
        assertEquals(CameraValueType.ISO, testInstance.unSelectableValueTypeFlow.value)
    }

    @Test
    fun test2() {
        testInstance.updateUserCameraValue(CameraValueType.SHUTTER_SPEED, 1/8000f)
        // ev 8.79
        // f16
        // 1/8000s
        // 51200
        assertEquals(CameraValueType.ISO, testInstance.unSelectableValueTypeFlow.value)
        val flow = testInstance.getUserCameraValueFlow(CameraValueType.ISO)
        assertEquals(51200f, flow.value)
    }
}