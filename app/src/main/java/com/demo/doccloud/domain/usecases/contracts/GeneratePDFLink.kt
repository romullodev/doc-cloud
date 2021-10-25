package com.demo.doccloud.domain.usecases.contracts

import android.net.Uri
import com.demo.doccloud.domain.entities.Doc

interface GeneratePDFLink {
    suspend operator fun invoke(doc: Doc) : Uri
}