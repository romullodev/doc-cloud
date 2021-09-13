package com.demo.doccloud.utils

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.MutableLiveData
import com.demo.doccloud.R
import com.demo.doccloud.domain.entities.DocStatus
import com.demo.doccloud.domain.entities.User
import java.io.File

class Global {
    companion object{
        var user = MutableLiveData<Event<User>>()

        /** Use external media if it is available, our app's file directory otherwise */
        fun getOutputDirectory(context: Context): File {
            //this code bellow only return our app's file directory
            //val appContext = context.applicationContext
            //return appContext.filesDir

            val appContext = context.applicationContext
            val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
                File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() } }
            return if (mediaDir != null && mediaDir.exists())
                mediaDir else appContext.filesDir
        }

        fun getInternalOutputDirectory(context: Context): File {
            //this code bellow only return our app's file directory
            val appContext = context.applicationContext
            return appContext.filesDir
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

        fun sharedPdfDoc(file: File, context: Context, act: AppCompatActivity){
            val shareIntent = Intent(Intent.ACTION_SEND)
            val outputPdfUri = FileProvider.getUriForFile(
                context,
                act.packageName.toString() + ".provider",
                file
            )
            shareIntent.putExtra(Intent.EXTRA_STREAM, outputPdfUri)
            shareIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            //Write Permission might not be necessary
            shareIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            shareIntent.type = "application/pdf"
            act.startActivity(Intent.createChooser(shareIntent, "Compartilhar com"))
        }
    }
}