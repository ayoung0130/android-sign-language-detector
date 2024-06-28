package com.example.sign_language_detector.ui.splash

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.sign_language_detector.R

private val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.CAMERA)

class SplashFragment : Fragment(R.layout.fragment_splash) {

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(
                    context,
                    "카메라 권한이 허용되었습니다",
                    Toast.LENGTH_SHORT
                ).show()
                navigateToHome()
            } else {
                Toast.makeText(
                    context,
                    "카메라 권한이 거부되었습니다",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (hasPermissions(requireContext())) {
            // 권한이 이미 있는 경우 1.5초 후 HomeFragment로 이동
            Handler(Looper.getMainLooper()).postDelayed({
                navigateToHome()
            }, 1500)
        } else {
            // 권한이 없는 경우 권한을 요청
            requestPermissionLauncher.launch(
                Manifest.permission.CAMERA
            )
        }
    }

    private fun navigateToHome() {
        lifecycleScope.launchWhenStarted {
            findNavController().navigate(R.id.action_splash_to_home)
        }
    }

    companion object {
        /** Convenience method used to check if all permissions required by this app are granted */
        fun hasPermissions(context: Context) = PERMISSIONS_REQUIRED.all {
            ContextCompat.checkSelfPermission(
                context,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}
