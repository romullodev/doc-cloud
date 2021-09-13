package com.demo.doccloud.domain.usecases.impl

import android.content.Context
import androidx.work.*
import com.demo.doccloud.domain.usecases.contracts.ScheduleToSyncData
import com.demo.doccloud.workers.SyncDataWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ScheduleToSyncDataImpl @Inject constructor(
    @ApplicationContext private val context: Context
): ScheduleToSyncData {
    override suspend fun invoke() {
        //setup constraint to workManager (only send if network is available)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        //setup the request work to send
        val syncData =
            OneTimeWorkRequestBuilder<SyncDataWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

        //schedule the work to be done
        WorkManager.getInstance(context).enqueue(syncData)
    }
}