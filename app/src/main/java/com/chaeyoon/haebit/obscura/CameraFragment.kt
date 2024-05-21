package com.chaeyoon.haebit.obscura

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.chaeyoon.haebit.R
import com.chaeyoon.haebit.databinding.FragmentCameraBinding
import com.chaeyoon.haebit.obscura.core.Camera
import com.chaeyoon.haebit.obscura.core.CameraImpl
import com.chaeyoon.haebit.permission.PermissionChecker

/**
 * CameraFragment
 */
class CameraFragment : Fragment(R.layout.fragment_camera) {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private lateinit var permissionChecker: PermissionChecker
    private val camera: Camera by lazy {
        CameraImpl(requireContext(), binding.cameraPreview, ::onCameraOpenFailed)
    }

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

        camera.startCamera(lifecycleScope)
    }

    private fun onCameraOpenFailed() {
        requireActivity().finish()
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

    companion object {
        private val TAG = CameraFragment::class.java.simpleName
    }
}