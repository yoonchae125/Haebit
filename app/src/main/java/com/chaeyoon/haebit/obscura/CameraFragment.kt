package com.chaeyoon.haebit.obscura

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.lifecycle.lifecycleScope
import com.chaeyoon.haebit.BuildConfig
import com.chaeyoon.haebit.R
import com.chaeyoon.haebit.databinding.FragmentCameraBinding
import com.chaeyoon.haebit.obscura.utils.constants.apertureValues
import com.chaeyoon.haebit.obscura.utils.constants.isoValues
import com.chaeyoon.haebit.obscura.utils.constants.shutterSpeedValues
import com.chaeyoon.haebit.obscura.utils.extensions.getVibrator
import com.chaeyoon.haebit.obscura.utils.extensions.launchAndCollect
import com.chaeyoon.haebit.obscura.utils.extensions.launchAndRepeatOnLifecycle
import com.chaeyoon.haebit.obscura.view.CameraValueListBinder
import com.chaeyoon.haebit.obscura.view.model.CameraValueType
import com.chaeyoon.haebit.obscura.view.model.LockRectUIState
import com.chaeyoon.haebit.obscura.view.model.Position
import com.chaeyoon.haebit.obscura.viewmodel.CameraFragmentViewModel
import com.chaeyoon.haebit.obscura.viewmodel.CameraValueListViewModel
import com.chaeyoon.haebit.permission.PermissionChecker
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * CameraFragment
 */
class CameraFragment : Fragment(R.layout.fragment_camera) {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CameraFragmentViewModel by lazy {
        ViewModelProvider(
            this,
            CameraFragmentViewModel.Factory(requireContext())
        ).get()
    }

    private lateinit var permissionChecker: PermissionChecker
    private val vibrator by lazy { requireContext().getVibrator() }

    private val mutex = Mutex()
    private var delayLockRectHideJob: Job? = null

    private val lockRectSize by lazy { resources.getDimension(R.dimen.camera_lock_rect_size) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)

        initPermissionChecker()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initCamera()
        initCameraValueListBinder()
        collectViewModel()

        if (BuildConfig.DEBUG) {
            displayDebugView()
        }
    }

    override fun onResume() {
        super.onResume()

        viewModel.startCamera()
    }

    override fun onStart() {
        super.onStart()
        permissionChecker.checkCameraPermissions()
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    private fun initPermissionChecker() {
        permissionChecker = PermissionChecker(this)
    }

    private fun initCamera() {
        viewModel.setCameraOutView(binding.cameraPreview, ::onCameraOpenFailed)
        setCameraLockButtonListeners()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setCameraLockButtonListeners() {
        binding.cameraPreview.setOnTouchListener { _, event ->
            val actionMasked = event.actionMasked
            if (actionMasked != MotionEvent.ACTION_DOWN) {
                return@setOnTouchListener false
            }
            vibrate()
            viewModel.lockCamera(event.x, event.y)
            false
        }

        binding.unlockButton.setOnClickListener {
            vibrate()
            viewModel.unlockCamera()
            vibrate()
        }
    }

    private fun vibrate() {
        val vibrator = vibrator?.takeIf(Vibrator::hasVibrator)
            ?: return

        val effect = VibrationEffect.createOneShot(
            VIBRATE_DURATION,
            VibrationEffect.DEFAULT_AMPLITUDE
        )
        vibrator.vibrate(effect)
    }

    private fun onCameraOpenFailed() {
        requireActivity().finish()
    }

    private fun initCameraValueListBinder() {
        val binderViewModel: CameraValueListViewModel =
            ViewModelProvider(
                this,
                CameraValueListViewModel.Factory(requireContext())
            ).get()

        CameraValueListBinder(
            requireContext(),
            viewLifecycleOwner,
            binding.apertureList,
            apertureValues,
            CameraValueType.APERTURE,
            binderViewModel,
            ::updateCenterValueText
        )
        CameraValueListBinder(
            requireContext(),
            viewLifecycleOwner,
            binding.shutterSpeedList,
            shutterSpeedValues,
            CameraValueType.SHUTTER_SPEED,
            binderViewModel,
            ::updateCenterValueText
        )
        CameraValueListBinder(
            requireContext(),
            viewLifecycleOwner,
            binding.isoList,
            isoValues,
            CameraValueType.ISO,
            binderViewModel,
            ::updateCenterValueText
        )
    }

    private fun updateCenterValueText(text: String) {
        binding.selectedCameraValueText.text = text
    }

    private fun collectViewModel() {
        viewLifecycleOwner.launchAndRepeatOnLifecycle {
            viewModel.exposureValueTextFlow.launchAndCollect(this, ::updateExposureValueText)

            viewModel.lockIconVisibilityFlow.launchAndCollect(this, ::updateUnlockButtonVisibility)

            viewModel.lockRectUIStateFlow.launchAndCollect(this, ::updateLockState)
        }
    }

    private fun updateExposureValueText(text: String) {
        binding.exposureValueText.text = text
    }

    private fun updateUnlockButtonVisibility(isVisible: Boolean) {
        binding.unlockButton.isVisible = isVisible
    }

    private fun updateLockState(lockUIState: LockRectUIState) {
        updateExposureTextColor(lockUIState is LockRectUIState.LockRectLockedState)
        when (lockUIState) {
            is LockRectUIState.LockRectProcessingState -> {
                updateLockRegionPosition(position = lockUIState.position)
                binding.lockRegion.setBackgroundResource(lockUIState.drawableRes)
                showRectFor()
            }

            is LockRectUIState.LockRectLockedState -> {
                updateLockRegionPosition(position = lockUIState.position)
                binding.lockRegion.setBackgroundResource(lockUIState.drawableRes)
                showRectFor(lockUIState.visibleTimeMillis)
            }

            is LockRectUIState.LockRectUnlockedState -> {
                hideRect()
            }
        }
    }

    private fun updateLockRegionPosition(position: Position) {
        binding.lockRegion.x = position.x - lockRectSize / 2
        binding.lockRegion.y = position.y - lockRectSize / 2
    }

    private fun updateExposureTextColor(isLocked: Boolean) {
        val textColor = if (isLocked) {
            requireContext().getColor(R.color.center_value_locked)
        } else {
            requireContext().getColor(R.color.center_value_normal)
        }
        binding.exposureValueText.setTextColor(textColor)
    }

    private fun showRectFor(visibleTimeMillis: Long = 0L) {
        viewLifecycleOwner.lifecycleScope.launch {
            mutex.withLock {
                delayLockRectHideJob?.cancel()
                binding.lockRegion.isVisible = true
                if (visibleTimeMillis > 0) {
                    // delay job
                    delayLockRectHideJob = launch {
                        delay(visibleTimeMillis)
                        hideRect()
                    }
                }
            }
        }
    }

    private fun hideRect() {
        binding.lockRegion.isVisible = false

    }

    private fun displayDebugView() {
        viewLifecycleOwner.launchAndRepeatOnLifecycle {
            combine(
                viewModel.isoFlow,
                viewModel.shutterSpeedFlow,
                viewModel.exposureValueFlow,
                viewModel.lensFocusDistanceFlow,
                viewModel.lockStateFlow
            ) { _, _, _, _, _ -> }.launchAndCollect(this) {
                updateText()
            }
        }
    }

    private fun updateText() {
        val text = "aperture ${viewModel.aperture}\n" +
                "iso ${viewModel.isoFlow.value}\n" +
                "shutterspeed ${viewModel.shutterSpeedFlow.value}\n" +
                "exposure ${viewModel.exposureValueFlow.value}\n" +
                "lens focus distance ${viewModel.lensFocusDistanceFlow.value}\n" +
                "lock state ${viewModel.lockStateFlow.value}"
        binding.debugView.text = text
    }

    companion object{
        private const val VIBRATE_DURATION = 2L
    }
}