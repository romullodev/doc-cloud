package com.demo.doccloud.domain.usecases.impl

import com.demo.doccloud.data.repository.Repository
import com.demo.doccloud.domain.entities.Photo
import com.demo.doccloud.domain.usecases.contracts.UpdateRemoteDocPhoto
import javax.inject.Inject

class UpdateRemoteDocPhotoImpl @Inject constructor(
    private val repository: Repository
): UpdateRemoteDocPhoto {
    override suspend fun invoke(remoteId: Long, photo: Photo)  = repository.updateRemoteDocPhoto(remoteId, photo)
}