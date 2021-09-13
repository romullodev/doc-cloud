package com.demo.doccloud.domain.usecases.impl

import com.demo.doccloud.data.repository.Repository
import com.demo.doccloud.domain.entities.Photo
import com.demo.doccloud.domain.usecases.contracts.DeleteLocalDocPhoto
import javax.inject.Inject

class DeleteLocalDocPhotoImpl @Inject constructor(
    private val repository: Repository
) : DeleteLocalDocPhoto {
    override suspend fun invoke(localId: Long, photo: Photo) =
        repository.deleteDocPhoto(localId = localId, photo = photo)

}