package com.demo.doccloud.domain.usecases.impl

import com.demo.doccloud.data.repository.Repository
import com.demo.doccloud.domain.usecases.contracts.UpdateLocalDocName
import javax.inject.Inject

class UpdateLocalDocNameImpl @Inject constructor(
    private val repository: Repository
) : UpdateLocalDocName {
    override suspend fun invoke(localId: Long, name: String) =
        repository.updateDocName(localId = localId, name = name)
}