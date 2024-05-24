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
import com.chaeyoon.haebit.obscura.core.Camera
import com.chaeyoon.haebit.obscura.core.CameraImpl
import com.chaeyoon.haebit.obscura.viewmodel.CameraFragmentViewModel
import com.chaeyoon.haebit.permission.PermissionChecker
import com.chaeyoon.haebit.obscura.utils.extensions.launchAndRepeatOnLifecycle
import com.chaeyoon.haebit.obscura.utils.extensions.toTwoDecimalPlaces

/**
 * CameraFragment
 */
class CameraFragment : Fragment(R.layout.fragment_camera) {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CameraFragmentViewModel by lazy {
        ViewModelProvider(
            requireActivity(),
            CameraFragmentViewModel.Factory(requireContext())
        ).get()
    }

    private lateinit var permissionChecker: PermissionChecker
    private val camera: Camera by lazy { CameraImpl(requireContext()) }

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
            viewModel.exposureValueFlow.collect {
                binding.exposureValueText.text = it.toEVTextFormat()
            }
        }
    }

    private fun Float.toEVTextFormat(): String = "EV ${toTwoDecimalPlaces()}"

    companion object {
        private val TAG = CameraFragment::class.java.simpleName
    }
}