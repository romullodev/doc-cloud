package com.demo.doccloud.domain.usecases.impl

import android.content.Context
import androidx.work.*
import com.demo.doccloud.domain.entities.Photo
import com.demo.doccloud.domain.usecases.contracts.ScheduleToDeleteRemoteDocPhoto
import com.demo.doccloud.utils.AppConstants
import com.demo.doccloud.workers.DeleteDocPageWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ScheduleToDeleteRemoteDocPhotoImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ScheduleToDeleteRemoteDocPhoto {
    override suspend fun invoke(localId: Long, photo: Photo) {
        val data =
            workDataOf(
                AppConstants.LOCAL_ID_KEY to localId,
                AppConstants.PHOTO_ID_KEY to photo.id,
                AppConstants.PHOTO_PATH_KEY to photo.path
            )

        //setup constraint to workManager (only send if network is available)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        //setup the request work to send
        val deleteDocPhotoWorkRequest =
            OneTimeWorkRequestBuilder<DeleteDocPageWorker>()
                .setInputData(data)
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

        //schedule the work to be done
        WorkManager.getInstance(context).enqueue(deleteDocPhotoWorkRequest)
    }
}