package com.demo.doccloud.domain.usecases.impl

import android.content.Context
import androidx.work.*
import com.demo.doccloud.domain.usecases.contracts.ScheduleToAddRemoteDocPhotos
import com.demo.doccloud.utils.AppConstants
import com.demo.doccloud.workers.AddDocPhotosWorker
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ScheduleToAddRemoteDocPhotosImpl @Inject constructor(
    @ApplicationContext private val context: Context
): ScheduleToAddRemoteDocPhotos {
    override suspend fun invoke(localId: Long, photosId: List<Long>) {
        val data =
            workDataOf(
                AppConstants.LOCAL_ID_KEY to localId,
                AppConstants.LIST_PHOTO_ADD_KEY to Gson().toJson(photosId),
            )

        //setup constraint to workManager (only send if network is available)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        //setup the request work to send
        val addPhotosWorkRequest =
            OneTimeWorkRequestBuilder<AddDocPhotosWorker>()
                .setInputData(data)
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

        //schedule the work to be done
        WorkManager.getInstance(context).enqueue(addPhotosWorkRequest)
    }
}