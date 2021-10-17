package com.demo.doccloud.domain.usecases.impl

import androidx.lifecycle.LiveData
import com.demo.doccloud.data.repository.Repository
import com.demo.doccloud.domain.entities.Doc
import com.demo.doccloud.domain.usecases.contracts.GetAllDocs
import javax.inject.Inject

class GetAllDocsImpl @Inject constructor(
    private val repository: Repository
): GetAllDocs {
    override fun invoke(): LiveData<List<Doc>> {
        return repository.docs
    }
}