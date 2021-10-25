package com.demo.doccloud.domain.usecases.impl

import android.net.Uri
import com.demo.doccloud.data.repository.Repository
import com.demo.doccloud.domain.entities.Doc
import com.demo.doccloud.domain.usecases.contracts.GenerateDocPdf
import com.demo.doccloud.domain.usecases.contracts.GeneratePDFLink
import com.demo.doccloud.domain.usecases.contracts.GetRemoveTempFileTime
import com.demo.doccloud.domain.usecases.contracts.ScheduleToRemoveTempFile
import javax.inject.Inject

class GeneratePDFLinkImpl @Inject constructor(
    private val generateDocPdfUseCase: GenerateDocPdf,
    private val scheduleToRemoveTempFileUseCase: ScheduleToRemoveTempFile,
    private val getRemoveTempFileTimeUseCase: GetRemoveTempFileTime,
    private val repository: Repository
) : GeneratePDFLink {
    override suspend fun invoke(doc: Doc): Uri {
        val pdfFile = generateDocPdfUseCase(doc)
        val timestamp = System.currentTimeMillis()
        val uri: Uri = repository.generatePDFLink(pdfFile, timestamp)
        pdfFile.delete()
        val delay = getRemoveTempFileTimeUseCase()
        //schedule to delete this file in $delay min using timestamp
        scheduleToRemoveTempFileUseCase(timestamp, delay)
        return uri
    }
}