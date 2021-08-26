package com.demo.doccloud.utils

import android.content.Context
import com.demo.doccloud.R
import com.demo.doccloud.domain.DocStatus
import com.demo.doccloud.domain.User
import java.io.File

class Global {
    companion object{
        var user: User? = null

        /** Use external media if it is available, our app's file directory otherwise */
        //return only our app's file directory
        fun getOutputDirectory(context: Context): File {
            //val appContext = context.applicationContext
            //return appContext.filesDir

            val appContext = context.applicationContext
            val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
                File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() } }
            return if (mediaDir != null && mediaDir.exists())
                mediaDir else appContext.filesDir


        }

        fun getDocStatus(status: DocStatus, context: Context) = when (status) {
            DocStatus.SENT -> {
                context.getString(R.string.home_doc_status_sent)
            }
            DocStatus.SENDING -> {
                context.getString(R.string.home_doc_status_sending)
            }
            DocStatus.NOT_SENT -> {
                context.getString(R.string.home_doc_status_not_sent)
            }

        }
    }
}