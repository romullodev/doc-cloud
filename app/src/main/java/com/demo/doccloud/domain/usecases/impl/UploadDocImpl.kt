package com.demo.doccloud.domain.usecases.impl

import com.demo.doccloud.data.repository.Repository
import com.demo.doccloud.domain.entities.Doc
import com.demo.doccloud.domain.usecases.contracts.UploadDoc
import javax.inject.Inject

class UploadDocImpl @Inject constructor(
    private val repository: Repository
) : UploadDoc {
    override suspend fun invoke(doc: Doc) =
        repository.uploadDoc(doc)
}