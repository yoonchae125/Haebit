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
import com.chaeyoon.haebit.R
import com.chaeyoon.haebit.databinding.FragmentCameraBinding
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

    private val debug = true

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
        if(debug){
            displayDebugView()
        }
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
        viewModel.startCamera()
        setCameraLockButtonListeners()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setCameraLockButtonListeners(){
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
            viewModel.exposureValueTextFlow.launchAndCollect(this) { text ->
                binding.exposureValueText.text = text
            }

            viewModel.lockIconVisibility.launchAndCollect(this) { isVisible ->
                binding.unlockButton.isVisible = isVisible
            }
        }
    }

    private fun displayDebugView(){
        viewLifecycleOwner.launchAndRepeatOnLifecycle {
            viewModel.isoFlow.launchAndCollect(this){
                updateText()
            }
            viewModel.shutterSpeedFlow.launchAndCollect(this){
                updateText()
            }
            viewModel.exposureValueFlow.launchAndCollect(this){
                updateText()
            }
            viewModel.lensFocusDistanceFlow.launchAndCollect(this){
                updateText()
            }
        }
    }

    private fun updateText(){
        binding.debugView.text = "aperture ${viewModel.aperture}\n" +
                "iso ${viewModel.isoFlow.value}\n" +
                "shutterspeed ${viewModel.shutterSpeedFlow.value}\n" +
                "exposure ${viewModel.exposureValueFlow.value}\n" +
                "lens focus distance ${viewModel.lensFocusDistanceFlow.value}\n"
    }
}