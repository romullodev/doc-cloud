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
        const val TIMESTAMP_FORMAT_BR = "dd-MM-yyyy HH:mm:ss:SSS"

        // Milliseconds used for UI animations
        const val ANIMATION_FAST_MILLIS = 50L
        const val ANIMATION_SLOW_MILLIS = 100L
        // Dialog tags
        const val INFO_DIALOG_TAG = "info.dialog.tag"
        const val QUESTION_DIALOG_TAG = "question.dialog.tag"
        // retrieve doc info on workManager
        const val LOCAL_ID_KEY = "local.id.key"
        const val REMOTE_ID_KEY = "remote.id.key"
        const val PAGES_NUMBER_KEY = "pages.number"
        const val PHOTO_ID_KEY = "photo.id.key"
        const val PHOTO_PATH_KEY = "photo.path.key"
        const val DOC_NAME_ID_KEY = "doc.name.id.key"

        //Firebase directories
        const val DATABASE_USERS_DIRECTORY = "users_database"
        const val DATABASE_DOCUMENTS_DIRECTORY = "documents"
        const val STORAGE_USERS_DIRECTORY = "users_storage"
        const val STORAGE_IMAGES_DIRECTORY = "images"
        const val DATABASE_DATE_KEY = "Date"
        const val DATABASE_REMOTE_ID_KEY = "RemoteId"
        const val DATABASE_DOC_NAME_KEY = "Name"
        const val DATABASE_TOTAL_PAGES_KEY = "PagesNumber"
    }
}