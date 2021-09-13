package com.demo.doccloud.domain.usecases.impl

import com.demo.doccloud.domain.entities.Photo
import com.demo.doccloud.domain.usecases.contracts.DeleteDocPhoto
import com.demo.doccloud.domain.usecases.contracts.DeleteLocalDocPhoto
import com.demo.doccloud.domain.usecases.contracts.ScheduleToDeleteRemoteDocPhoto
import javax.inject.Inject

class DeleteDocPhotoImpl @Inject constructor(
    private val deleteLocalDocPhoto: DeleteLocalDocPhoto,
    private val scheduleToDeleteRemoteDocPhoto: ScheduleToDeleteRemoteDocPhoto
) : DeleteDocPhoto {
    override suspend fun invoke(localId: Long, photo: Photo) {
        deleteLocalDocPhoto(localId = localId, photo = photo)
        scheduleToDeleteRemoteDocPhoto(localId = localId, photo = photo)
    }
}