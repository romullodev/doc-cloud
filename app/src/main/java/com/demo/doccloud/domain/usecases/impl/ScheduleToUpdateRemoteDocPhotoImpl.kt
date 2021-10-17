package com.demo.doccloud.domain.usecases.impl

import android.content.Context
import androidx.work.*
import com.demo.doccloud.domain.entities.Photo
import com.demo.doccloud.domain.usecases.contracts.ScheduleToUpdateRemoteDocPhoto
import com.demo.doccloud.utils.AppConstants
import com.demo.doccloud.workers.UpdateDocPageWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ScheduleToUpdateRemoteDocPhotoImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ScheduleToUpdateRemoteDocPhoto {
    override suspend fun invoke(localId: Long, photo: Photo) {
        val data =
            workDataOf(
                AppConstants.LOCAL_ID_KEY to localId,
                AppConstants.PHOTO_ID_KEY to photo.id,
                AppConstants.PHOTO_PATH_KEY to photo.path,
            )

        //setup constraint to workManager (only send if network is available)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        //setup the request work to send
        val updateDocNameWorkRequest =
            OneTimeWorkRequestBuilder<UpdateDocPageWorker>()
                .setInputData(data)
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .addTag("${localId}_${photo.id}")
                .build()
        //cancel a schedule work in case it has been previous setup by this same localId + photo tag
        //this code could be improved
        WorkManager.getInstance(context).cancelAllWorkByTag("${localId}_${photo.id}")

        //schedule the work to be done
        WorkManager.getInstance(context).enqueue(updateDocNameWorkRequest)
    }
}