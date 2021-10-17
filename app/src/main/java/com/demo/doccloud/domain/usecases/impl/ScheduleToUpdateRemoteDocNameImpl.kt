package com.demo.doccloud.domain.usecases.impl

import android.content.Context
import androidx.work.*
import com.demo.doccloud.domain.usecases.contracts.ScheduleToUpdateRemoteDocName
import com.demo.doccloud.utils.AppConstants
import com.demo.doccloud.workers.UpdateDocNameWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ScheduleToUpdateRemoteDocNameImpl @Inject constructor(
    @ApplicationContext private val context: Context,
): ScheduleToUpdateRemoteDocName {
    override suspend fun invoke(localId: Long, remoteId: Long, name: String) {
        val data =
            workDataOf(
                AppConstants.LOCAL_ID_KEY to localId,
                AppConstants.REMOTE_ID_KEY to remoteId,
                AppConstants.DOC_NAME_ID_KEY to name,

                )

        //setup constraint to workManager (only send if network is available)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        //setup the request work to send
        val updateDocNameWorkRequest =
            OneTimeWorkRequestBuilder<UpdateDocNameWorker>()
                .setInputData(data)
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .addTag("$remoteId")
                .build()

        //cancel a schedule work in case it has been previous setup by this same remoteId tag
        //this code could be improved
        WorkManager.getInstance(context).cancelAllWorkByTag("$remoteId")

        //schedule the work to be done
        WorkManager.getInstance(context).enqueue(updateDocNameWorkRequest)
    }
}