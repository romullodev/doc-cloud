package com.demo.doccloud.domain.usecases.impl

import com.demo.doccloud.data.repository.Repository
import com.demo.doccloud.domain.entities.Doc
import com.demo.doccloud.domain.usecases.contracts.UpdateLocalDoc
import javax.inject.Inject

class UpdateLocalDocImpl @Inject constructor(
    private val repository: Repository
): UpdateLocalDoc {
    override suspend fun invoke(doc: Doc) = repository.updateLocalDoc(doc)
}