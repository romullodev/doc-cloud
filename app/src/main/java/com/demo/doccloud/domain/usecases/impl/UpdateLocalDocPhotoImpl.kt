package com.demo.doccloud.domain.usecases.impl

import com.demo.doccloud.data.repository.Repository
import com.demo.doccloud.domain.entities.Photo
import com.demo.doccloud.domain.usecases.contracts.UpdateLocalDocPhoto
import javax.inject.Inject

class UpdateLocalDocPhotoImpl @Inject constructor(
    private val repository: Repository,
) : UpdateLocalDocPhoto {
    override suspend fun invoke(localId: Long, photo: Photo) = repository.updateDocPhoto(localId = localId, photo = photo)
}