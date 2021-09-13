package com.demo.doccloud.domain.usecases.impl

import com.demo.doccloud.data.repository.Repository
import com.demo.doccloud.domain.entities.Doc
import com.demo.doccloud.domain.usecases.contracts.SaveLocalDoc
import javax.inject.Inject

class SaveLocalDocImpl @Inject constructor(
    private val repository: Repository
) : SaveLocalDoc {
    override suspend fun invoke(doc: Doc) = repository.saveDoc(doc)
}