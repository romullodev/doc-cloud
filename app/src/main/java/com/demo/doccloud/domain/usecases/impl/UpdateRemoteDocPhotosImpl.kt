package com.demo.doccloud.domain.usecases.impl

import com.demo.doccloud.data.repository.Repository
import com.demo.doccloud.domain.entities.Photo
import com.demo.doccloud.domain.usecases.contracts.UpdateRemoteDocPhotos
import javax.inject.Inject

class UpdateRemoteDocPhotosImpl @Inject constructor(
    private val repository: Repository
): UpdateRemoteDocPhotos {
    override suspend fun invoke(remoteId: Long, photo: Photo)  = repository.updateRemoteDocPhotos(remoteId, photo)
}