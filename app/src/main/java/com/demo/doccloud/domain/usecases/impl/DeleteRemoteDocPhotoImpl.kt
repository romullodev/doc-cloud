package com.demo.doccloud.domain.usecases.impl

import com.demo.doccloud.data.repository.Repository
import com.demo.doccloud.domain.entities.Photo
import com.demo.doccloud.domain.usecases.contracts.DeleteRemoteDocPhoto
import javax.inject.Inject

class DeleteRemoteDocPhotoImpl @Inject constructor(
    private val repository: Repository
) : DeleteRemoteDocPhoto {
    override suspend fun invoke(remoteId: Long, photo: Photo, jsonPages: String) =
        repository.deleteDocPhotosFirebase(remoteId, photo, jsonPages)

}