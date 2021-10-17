package com.demo.doccloud.domain.usecases.impl

import com.demo.doccloud.data.repository.Repository
import com.demo.doccloud.domain.usecases.contracts.SyncData
import javax.inject.Inject

class SyncDataImpl @Inject constructor(
    private val repository: Repository
): SyncData {
    override suspend fun invoke(customId: Long){
        val docs = repository.syncData(customId)
        repository.clearDocs()
        repository.insertDocs(docs)
    }
}