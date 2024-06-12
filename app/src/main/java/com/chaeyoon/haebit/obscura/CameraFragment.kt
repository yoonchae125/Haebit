package com.chaeyoon.haebit.obscura

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.lifecycle.lifecycleScope
import com.chaeyoon.haebit.R
import com.chaeyoon.haebit.databinding.FragmentCameraBinding
import com.chaeyoon.haebit.obscura.utils.constants.apertureValues
import com.chaeyoon.haebit.obscura.utils.constants.isoValues
import com.chaeyoon.haebit.obscura.utils.constants.shutterSpeedValues
import com.chaeyoon.haebit.obscura.utils.extensions.launchAndCollect
import com.chaeyoon.haebit.obscura.utils.extensions.launchAndRepeatOnLifecycle
import com.chaeyoon.haebit.obscura.utils.extensions.toTwoDecimalPlaces
import com.chaeyoon.haebit.obscura.view.CameraValueListBinder
import com.chaeyoon.haebit.obscura.view.model.CameraValueType
import com.chaeyoon.haebit.obscura.viewmodel.CameraFragmentViewModel
import com.chaeyoon.haebit.permission.PermissionChecker

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

        viewModel.setCameraOutView(binding.cameraPreview, ::onCameraOpenFailed)
        viewModel.startCamera(lifecycleScope)

        collectViewModel()

        initCameraValueListBinder()
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

    private fun onCameraOpenFailed() {
        requireActivity().finish()
    }

    private fun collectViewModel() {
        viewLifecycleOwner.launchAndRepeatOnLifecycle {
            viewModel.exposureValueFlow.launchAndCollect(this) {
                binding.exposureValueText.text = it.toEVTextFormat()
            }
            viewModel.unSelectableCameraValueTextFlow.launchAndCollect(this) {
                binding.selectedCameraValueText.text = it
            }
        }
    }

    private fun initCameraValueListBinder() {
        CameraValueListBinder(
            requireContext(),
            viewLifecycleOwner,
            binding.apertureList,
            apertureValues,
            CameraValueType.APERTURE,
            viewModel
        )
        CameraValueListBinder(
            requireContext(),
            viewLifecycleOwner,
            binding.shutterSpeedList,
            shutterSpeedValues,
            CameraValueType.SHUTTER_SPEED,
            viewModel
        )
        CameraValueListBinder(
            requireContext(),
            viewLifecycleOwner,
            binding.isoList,
            isoValues,
            CameraValueType.ISO,
            viewModel
        )
    }

    private fun Float.toEVTextFormat(): String = "EV ${toTwoDecimalPlaces()}"

    companion object {
        private val TAG = CameraFragment::class.java.simpleName
    }
}

