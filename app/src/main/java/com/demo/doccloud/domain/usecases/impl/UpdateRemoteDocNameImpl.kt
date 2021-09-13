package com.demo.doccloud.domain.usecases.impl

import com.demo.doccloud.data.repository.Repository
import com.demo.doccloud.domain.usecases.contracts.UpdateRemoteDocName
import javax.inject.Inject

class UpdateRemoteDocNameImpl @Inject constructor(
    private val repository: Repository
) : UpdateRemoteDocName {
    override suspend fun invoke(remoteId: Long, name: String) =
        repository.updateRemoteDocName(remoteId, name)

}