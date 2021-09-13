package com.demo.doccloud.domain.usecases.impl

import com.demo.doccloud.data.repository.Repository
import com.demo.doccloud.domain.entities.Photo
import com.demo.doccloud.domain.usecases.contracts.AddPhotoToRemoteDoc
import javax.inject.Inject

class AddPhotoToRemoteDocImpl @Inject constructor(
    private val repository: Repository
) : AddPhotoToRemoteDoc {
    override suspend fun invoke(remoteId: Long, photos: List<Photo>, newJsonPages: String) =
        repository.addPhotosToRemoteDoc(remoteId, photos, newJsonPages)

}