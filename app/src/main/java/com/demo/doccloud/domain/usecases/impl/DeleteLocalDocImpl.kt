package com.demo.doccloud.domain.usecases.impl

import com.demo.doccloud.data.repository.Repository
import com.demo.doccloud.domain.entities.Doc
import com.demo.doccloud.domain.usecases.contracts.DeleteLocalDoc
import javax.inject.Inject

class DeleteLocalDocImpl @Inject constructor(
    private val repository: Repository
): DeleteLocalDoc {
    override suspend fun invoke(doc: Doc) = repository.deleteDoc(doc)
}