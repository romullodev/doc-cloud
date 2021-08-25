package com.demo.doccloud.utils

import android.Manifest

class AppConstants {
    companion object {
        val APP_PERMISSIONS = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        )

        const val IMMERSIVE_FLAG_TIMEOUT = 500L

        /** Milliseconds used for UI animations */
        const val ANIMATION_FAST_MILLIS = 50L
        const val ANIMATION_SLOW_MILLIS = 100L

        const val INFO_DIALOG_TAG = "info.dialog.tag"
        const val QUESTION_DIALOG_TAG = "question.dialog.tag"
    }
}