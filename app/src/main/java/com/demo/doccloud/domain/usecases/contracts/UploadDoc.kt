package com.demo.doccloud.domain.usecases.contracts

import com.demo.doccloud.domain.entities.Doc

interface UploadDoc  {
    suspend operator fun invoke(doc: Doc)
}