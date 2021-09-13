package com.demo.doccloud.domain.usecases.impl

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.demo.doccloud.di.IoDispatcher
import com.demo.doccloud.domain.usecases.contracts.CopyFile
import com.demo.doccloud.utils.Global
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import javax.inject.Inject

//https://stackoverflow.com/questions/57093479/get-real-path-from-uri-data-is-deprecated-in-android-q
class CopyFileImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : CopyFile {
    override suspend fun invoke(uri: Uri) = withContext(dispatcher) {
        val contentResolver: ContentResolver = context.contentResolver ?: return@withContext null
        // Create file path inside app's data dir
        val filePath =
            "${Global.getInternalOutputDirectory(context)}${File.separator}${System.currentTimeMillis()}"
        val file = File(filePath)
        try {
            val inputStream = contentResolver.openInputStream(uri) ?: return@withContext null
            val outputStream: OutputStream = FileOutputStream(file)
            val buf = ByteArray(1024)
            var len: Int
            while (inputStream.read(buf).also { len = it } > 0) outputStream.write(
                buf,
                0,
                len
            )
            outputStream.close()
            inputStream.close()
        } catch (e: IOException) {
            throw e
        }
        return@withContext file
    }
}