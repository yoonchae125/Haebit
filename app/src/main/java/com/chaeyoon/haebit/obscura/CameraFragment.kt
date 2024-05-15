package com.chaeyoon.haebit.obscura

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.chaeyoon.haebit.R
import com.chaeyoon.haebit.databinding.FragmentCameraBinding
import com.chaeyoon.haebit.permission.PermissionChecker

/**
 * CameraFragment
 */
class CameraFragment : Fragment(R.layout.fragment_camera) {

    private val binding by lazy { FragmentCameraBinding.inflate(layoutInflater) }

    private lateinit var permissionChecker: PermissionChecker

    private val finishRequestKey by lazy {
        requireNotNull(
            arguments?.getString(ARG_FINISH_REQUEST_KEY)
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        permissionChecker = PermissionChecker(this)
    }

    override fun onStart() {
        super.onStart()
        permissionChecker.checkCameraPermissions()
    }

    private fun finishCamera() {
        setFragmentResult(finishRequestKey, bundleOf())
    }

    companion object {
        fun newInstance(
            finishResultKey: String
        ) = CameraFragment().apply {
            arguments = bundleOf(ARG_FINISH_REQUEST_KEY to finishResultKey)
        }

        private const val ARG_FINISH_REQUEST_KEY = "ARG_FINISH_REQUEST_KEY"
    }
}