package com.chaeyoon.haebit

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.commit
import com.chaeyoon.haebit.databinding.ActivityMainBinding
import com.chaeyoon.haebit.obscura.CameraFragment

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initFragmentResultListener()
        openCameraFragment()
    }

    private fun initFragmentResultListener() {
        supportFragmentManager.setFragmentResultListener(
            REQUEST_KEY_FINISH,
            this
        ) { key, _ ->
            if (key != REQUEST_KEY_FINISH) return@setFragmentResultListener
            finish()
        }
    }

    private fun openCameraFragment() {
        supportFragmentManager.commit {
            val cameraFragment = CameraFragment.newInstance(REQUEST_KEY_FINISH)
            replace(R.id.fragment_container, cameraFragment)
        }
    }

    companion object {
        private const val REQUEST_KEY_FINISH = "REQUEST_KEY_FINISH"
    }
}