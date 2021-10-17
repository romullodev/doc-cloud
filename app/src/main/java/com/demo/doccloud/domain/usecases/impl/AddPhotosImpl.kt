package com.demo.doccloud.domain.usecases.impl

import com.demo.doccloud.domain.entities.Photo
import com.demo.doccloud.domain.usecases.contracts.AddPhotos
import com.demo.doccloud.domain.usecases.contracts.AddPhotosToLocalDoc
import com.demo.doccloud.domain.usecases.contracts.ScheduleToAddRemoteDocPhotos
import javax.inject.Inject

class AddPhotosImpl @Inject constructor(
    private val addPhotosToLocalDoc: AddPhotosToLocalDoc,
    private val scheduleToAddRemoteDocPhotos: ScheduleToAddRemoteDocPhotos
): AddPhotos {
    override suspend fun invoke(localId: Long, photos: List<Photo>) {
        addPhotosToLocalDoc(localId = localId, photos = photos)
        scheduleToAddRemoteDocPhotos(localId = localId, photosId = photos.map { it.id })
    }
}