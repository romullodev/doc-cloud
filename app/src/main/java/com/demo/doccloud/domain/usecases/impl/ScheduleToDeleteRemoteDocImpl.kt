package com.demo.doccloud.domain.usecases.impl

import android.content.Context
import androidx.work.*
import com.demo.doccloud.domain.usecases.contracts.ScheduleToDeleteRemoteDoc
import com.demo.doccloud.utils.AppConstants
import com.demo.doccloud.workers.DeleteDocWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ScheduleToDeleteRemoteDocImpl @Inject constructor(
    @ApplicationContext private val context: Context
): ScheduleToDeleteRemoteDoc {
    override suspend fun invoke(remoteId: Long, jsonPages: String) {
        val data =
            workDataOf(
                AppConstants.REMOTE_ID_KEY to remoteId,
                AppConstants.JSON_PAGES_KEY to jsonPages,
            )

        //setup constraint to workManager (only send if network is available)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        //setup the request work to send
        val deleteDocWorkRequest =
            OneTimeWorkRequestBuilder<DeleteDocWorker>()
                .setInputData(data)
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

        //schedule the work to be done
        WorkManager.getInstance(context).enqueue(deleteDocWorkRequest)
    }
}