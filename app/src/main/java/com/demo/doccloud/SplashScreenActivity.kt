package com.demo.doccloud

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.demo.doccloud.databinding.ActivitySplashScreenBinding
import com.demo.doccloud.ui.MainActivity
import com.demo.doccloud.utils.AppConstants.Companion.APP_PERMISSIONS
import com.demo.doccloud.utils.checkAllSelfPermissionsCompat
import com.demo.doccloud.utils.requestPermissionsCompat
import com.demo.doccloud.utils.shouldShowAllRequestPermissionsRationaleCompat
import timber.log.Timber

class SplashScreenActivity : AppCompatActivity() {

    private lateinit var _binding: ActivitySplashScreenBinding
    private val binding: ActivitySplashScreenBinding get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setFullScreenMode()
        //Set full screen after setting layout content
        initMainActivity()
    }

    private fun initMainActivity() {

        Handler(Looper.getMainLooper()).postDelayed({
            if (checkAppPermissions()) {
                // start MainActivity
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                // Permission is missing and must be requested.
                requestAppPermissions()
            }

        }, 1000)
    }

    private fun checkAppPermissions(): Boolean {
        // Check if the app permissions have been granted
        return checkAllSelfPermissionsCompat(APP_PERMISSIONS)
    }

    private fun requestAppPermissions() {
        if (shouldShowAllRequestPermissionsRationaleCompat(APP_PERMISSIONS)) {
            requestPermissionsCompat(
                APP_PERMISSIONS,
                PERMISSION_REQUEST_APP
            )
        } else {
            requestPermissionsCompat(
                APP_PERMISSIONS,
                PERMISSION_REQUEST_APP
            )
        }
    }

    private fun setFullScreenMode() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = window.insetsController

            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_APP) {
            if (grantResults.isNotEmpty() ) {
                grantResults.forEach {
                    if (it != PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(this, "Permissões não concedidas pelo usuário!", Toast.LENGTH_SHORT).show()
                        finish()
                        return
                    }
                }
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Ocorreu um erro nas permissões do aplicativo!", Toast.LENGTH_LONG).show()
                Timber.i("grantResults array is empty")
                finish()
            }
        }
    }

    companion object {
        const val PERMISSION_REQUEST_APP = 0
    }
}