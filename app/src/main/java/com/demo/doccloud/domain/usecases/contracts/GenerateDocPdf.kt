package com.demo.doccloud.domain.usecases.contracts

import com.demo.doccloud.domain.entities.Doc
import java.io.File

interface GenerateDocPdf {
    suspend operator fun invoke(doc: Doc) : File
}