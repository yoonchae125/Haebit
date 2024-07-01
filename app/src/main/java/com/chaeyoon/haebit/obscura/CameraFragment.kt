package com.chaeyoon.haebit.obscura

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import com.chaeyoon.haebit.BuildConfig
import com.chaeyoon.haebit.R
import com.chaeyoon.haebit.databinding.FragmentCameraBinding
import com.chaeyoon.haebit.obscura.core.LockState
import com.chaeyoon.haebit.obscura.utils.constants.apertureValues
import com.chaeyoon.haebit.obscura.utils.constants.isoValues
import com.chaeyoon.haebit.obscura.utils.constants.shutterSpeedValues
import com.chaeyoon.haebit.obscura.utils.extensions.launchAndCollect
import com.chaeyoon.haebit.obscura.utils.extensions.launchAndRepeatOnLifecycle
import com.chaeyoon.haebit.obscura.view.CameraValueListBinder
import com.chaeyoon.haebit.obscura.view.model.CameraValueType
import com.chaeyoon.haebit.obscura.viewmodel.CameraFragmentViewModel
import com.chaeyoon.haebit.obscura.viewmodel.CameraValueListViewModel
import com.chaeyoon.haebit.permission.PermissionChecker
import kotlinx.coroutines.flow.combine

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
            viewModel.lockCamera(event.x, event.y)
            false
        }

        binding.unlockButton.setOnClickListener {
            viewModel.unlockCamera()
        }
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

            viewModel.lockIconVisibility.launchAndCollect(this, ::updateUnlockButtonVisibility)

            viewModel.lockStateFlow.launchAndCollect(this, ::lockState)
        }
    }

    private fun updateExposureValueText(text: String) {
        binding.exposureValueText.text = text
    }

    private fun updateUnlockButtonVisibility(isVisible: Boolean) {
        binding.unlockButton.isVisible = isVisible
    }

    private fun lockState(lockState: LockState) {
        when (lockState) {
            LockState.LOCK_PROCESSING -> {
                // TODO: show processing rect
            }
            LockState.LOCKED -> {
                // TODO: show locked rect
            }
            LockState.UNLOCKED -> {
                // TODO: hide rect
            }
        }
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
}