package com.demo.doccloud.utils

import android.Manifest

class AppConstants {
    companion object {
        val APP_PERMISSIONS = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        )
    }
}