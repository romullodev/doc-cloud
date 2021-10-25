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
        const val INTENT_PDF_TYPE = "application/pdf"

        // previousBackStackEntry keys
        const val USER_SIGN_UP_KEY = "user.sign.up.key"

        // Milliseconds used for UI animations
        const val ANIMATION_FAST_MILLIS = 50L
        const val ANIMATION_SLOW_MILLIS = 100L
        // Dialog tags
        const val INFO_DIALOG_TAG = "info.dialog.tag"
        const val QUESTION_DIALOG_TAG = "question.dialog.tag"
        // retrieve doc info on workManager
        const val LOCAL_ID_KEY = "local.id.key"
        const val REMOTE_ID_KEY = "remote.id.key"
        const val JSON_PAGES_KEY = "json.pages.key"
        const val PHOTO_ID_KEY = "photo.id.key"
        const val PHOTO_PATH_KEY = "photo.path.key"
        const val DOC_NAME_ID_KEY = "doc.name.id.key"
        const val LIST_PHOTO_ADD_KEY = "list.photo.add.key"
        const val CUSTOM_ID_KEY = "custom.id.key"
        //Firebase directories
        const val DATABASE_USERS_DIRECTORY = "users_database"
        const val DATABASE_DOCUMENTS_DIRECTORY = "documents"
        const val STORAGE_USERS_DIRECTORY = "users_storage"
        const val STORAGE_IMAGES_DIRECTORY = "images"
        const val STORAGE_TEMP_DIRECTORY = "temporary"
        //Firebase fields for each doc
        const val DATABASE_DATE_KEY = "Date"
        const val DATABASE_REMOTE_ID_KEY = "RemoteId"
        const val DATABASE_DOC_NAME_KEY = "Name"
        const val DATABASE_JSON_PAGES_KEY = "JsonPages"
        //Firebase fields for sync strategy model
        const val DATABASE_APP_LEVEL_STRATEGY_KEY = "strategy"
        const val DATABASE_APP_LEVEL_EXPIRATION_KEY = "Sync Data Strategy Expiration"
        const val DATABASE_APP_LEVEL_EXCLUDE_TEMP_TIME_KEY = "Exclude Temp Files Time"
        const val REMOTE_DATABASE_CUSTOM_ID_KEY = "Custom ID" //localed on users level
        const val DATABASE_LAST_UPDATED_KEY = "Last Updated" //localed on users level
        const val DATABASE_SYNC_STRATEGY_KEY = "Sync Data Strategy" //key for access sync directory localed on users level
        // Default id value in case of login on new device
        const val DATABASE_DEFAULT_CUSTOM_ID = -1L
        const val LOCAL_DATABASE_CUSTOM_ID_KEY = "local.database.custom.id.key"
        //folder name of pdf directory (files-dir)
        const val PDF_FOLDER_NAME = "shared_pdfs" // if you change this name, don't forget to change on provider_path.xml too
    }
}