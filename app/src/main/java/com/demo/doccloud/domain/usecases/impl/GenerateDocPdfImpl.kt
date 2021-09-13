package com.demo.doccloud.domain.usecases.impl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import com.demo.doccloud.di.IoDispatcher
import com.demo.doccloud.domain.entities.Doc
import com.demo.doccloud.domain.usecases.contracts.GenerateDocPdf
import com.demo.doccloud.utils.Global
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class GenerateDocPdfImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : GenerateDocPdf {
    override suspend fun invoke(doc: Doc) = withContext(dispatcher) {
        val dir = Global.getInternalOutputDirectory(context)
        //save pdf
        var bitmap: Bitmap
        var pageInfo: PdfDocument.PageInfo
        val document = PdfDocument()
        var page: PdfDocument.Page
        var canvas: Canvas
        //calculates the larger width amongst photos
        var majorWidth = Int.MIN_VALUE
        doc.pages.forEach {
            bitmap = BitmapFactory.decodeFile(it.path)
            if (bitmap.width > majorWidth)
                majorWidth = bitmap.width
        }

        //create pdf with photos
        for ((index, path) in doc.pages.withIndex()) {
            bitmap = BitmapFactory.decodeFile(path.path)
            //bitmap = ImageResizer.reduceBitmapSize(bitmap, MAX_SIZE_EACH_PHOTO_DOCUMENT)
            pageInfo =
                PdfDocument.PageInfo.Builder(majorWidth, bitmap.height, index + 1) //A4 resolution
                    .create()
            page = document.startPage(pageInfo)
            canvas = page.canvas
            //bitmap = Bitmap.createScaledBitmap(
            //    bitmap,
            //    2480,
            //    3508,
            //    true
            //)
            val startPointCenter = (majorWidth - bitmap.width) / 2.0f
            canvas.drawBitmap(bitmap, startPointCenter, 0f, null)
            document.finishPage(page)
        }
        // Create the pdf name + timeStamp
        val formattedTimeStamp = SimpleDateFormat(
            "yyyy-MM-dd-HH-mm-ss-SSS",
            Locale.US
        ).format(System.currentTimeMillis())
        val pdfFilePath = "${dir.path}/${doc.name} ${formattedTimeStamp}.pdf"
        // write the document content
        val pdfFile = File(pdfFilePath)
        //this code bellow can throw an exception (up to document.close())
        //use a try catch block when invoke this function
        try {
            document.writeTo(FileOutputStream(pdfFile))
            document.close()
            return@withContext pdfFile
        } catch (e: Exception) {
            throw e
        }
    }
}