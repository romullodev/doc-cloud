package com.demo.doccloud.domain.usecases.impl

import android.content.Context
import androidx.work.*
import com.demo.doccloud.domain.usecases.contracts.ScheduleToRemoveTempFile
import com.demo.doccloud.utils.AppConstants
import com.demo.doccloud.workers.RemoveTempFileWorker
import com.demo.doccloud.workers.UploadDocWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ScheduleToRemoveTempFileImpl @Inject constructor(
    @ApplicationContext private val context: Context
): ScheduleToRemoveTempFile {
    override suspend fun invoke(customId: Long, delay: Long) {
        val data =
            workDataOf(
                AppConstants.CUSTOM_ID_KEY to customId,
            )

        //setup constraint to workManager (only send if network is available)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        //setup the request work to send
        val removeTempFileRequest =
            OneTimeWorkRequestBuilder<RemoveTempFileWorker>()
                .setInitialDelay(delay, TimeUnit.MINUTES)
                .setInputData(data)
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

        //schedule the work to be done
        WorkManager.getInstance(context).enqueue(removeTempFileRequest)
    }
}