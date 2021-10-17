package com.demo.doccloud.domain.usecases.impl

import com.demo.doccloud.data.repository.Repository
import com.demo.doccloud.domain.entities.Photo
import com.demo.doccloud.domain.usecases.contracts.AddPhotosToLocalDoc
import javax.inject.Inject

class AddPhotosToLocalDocImpl @Inject constructor(
    private val repository: Repository
) : AddPhotosToLocalDoc {
    override suspend fun invoke(localId: Long, photos: List<Photo>) {
        repository.addPhotos(localId, photos)
    }
}