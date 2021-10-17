package com.demo.doccloud.domain.usecases.impl

import com.demo.doccloud.domain.entities.Photo
import com.demo.doccloud.domain.usecases.contracts.ScheduleToUpdateRemoteDocPhoto
import com.demo.doccloud.domain.usecases.contracts.UpdateDocPhoto
import com.demo.doccloud.domain.usecases.contracts.UpdateLocalDocPhoto
import javax.inject.Inject

class UpdateDocPhotoImpl @Inject constructor(
    private val updateLocalDocPhoto : UpdateLocalDocPhoto,
    private val scheduleToUpdateRemoteDocPhoto: ScheduleToUpdateRemoteDocPhoto
) : UpdateDocPhoto {
    override suspend fun invoke(localId: Long, photo: Photo) {
        updateLocalDocPhoto(localId, photo)
        scheduleToUpdateRemoteDocPhoto(localId, photo)
    }
}