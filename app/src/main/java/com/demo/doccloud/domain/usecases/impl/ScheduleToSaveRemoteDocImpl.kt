package com.demo.doccloud.domain.usecases.impl

import android.content.Context
import androidx.work.*
import com.demo.doccloud.domain.usecases.contracts.ScheduleToSaveRemoteDoc
import com.demo.doccloud.utils.AppConstants
import com.demo.doccloud.workers.UploadDocWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ScheduleToSaveRemoteDocImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ScheduleToSaveRemoteDoc {
    override suspend fun invoke(rowNumber: Long) {
        val data =
            workDataOf(
                AppConstants.LOCAL_ID_KEY to rowNumber,
            )

        //setup constraint to workManager (only send if network is available)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        //setup the request work to send
        val uploadWorkRequest =
            OneTimeWorkRequestBuilder<UploadDocWorker>()
                .setInputData(data)
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

        //schedule the work to be done
        WorkManager.getInstance(context).enqueue(uploadWorkRequest)
    }
}