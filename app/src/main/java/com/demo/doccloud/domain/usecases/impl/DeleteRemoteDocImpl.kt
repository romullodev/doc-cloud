package com.demo.doccloud.domain.usecases.impl

import com.demo.doccloud.data.repository.Repository
import com.demo.doccloud.domain.entities.Photo
import com.demo.doccloud.domain.usecases.contracts.DeleteRemoteDoc
import javax.inject.Inject

class DeleteRemoteDocImpl @Inject constructor(
    private val repository: Repository
): DeleteRemoteDoc {
    override suspend fun invoke(remoteId: Long, pages: List<Photo>) = repository.deleteDocFirebase(remoteId, pages)
}