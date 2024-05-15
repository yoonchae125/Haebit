package com.chaeyoon.haebit.obscura

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import com.chaeyoon.haebit.R
import com.chaeyoon.haebit.databinding.FragmentCameraBinding
import com.chaeyoon.haebit.permission.PermissionChecker

/**
 * CameraFragment
 */
class CameraFragment : Fragment(R.layout.fragment_camera) {

    private val binding by lazy { FragmentCameraBinding.inflate(layoutInflater) }

    private lateinit var permissionChecker: PermissionChecker

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        permissionChecker = PermissionChecker(this)
    }

    override fun onStart() {
        super.onStart()
        permissionChecker.checkCameraPermissions()
    }
}