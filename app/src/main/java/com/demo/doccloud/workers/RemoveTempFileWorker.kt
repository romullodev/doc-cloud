package com.demo.doccloud.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.demo.doccloud.di.IoDispatcher
import com.demo.doccloud.domain.entities.DocStatus
import com.demo.doccloud.domain.usecases.contracts.GetDocById
import com.demo.doccloud.domain.usecases.contracts.RemoveTempFile
import com.demo.doccloud.domain.usecases.contracts.UpdateLocalDoc
import com.demo.doccloud.domain.usecases.contracts.UpdateRemoteDocName
import com.demo.doccloud.utils.AppConstants
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber

@HiltWorker
class RemoveTempFileWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
    private val removeTempFileUseCase: RemoveTempFile
) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(dispatcher) {
            // Retrieve customId
            val customId: Long = inputData.getLong(AppConstants.CUSTOM_ID_KEY, -1L)
            try {
                //in case of deletion
                if (customId != -1L) {
                    removeTempFileUseCase(customId)
                    return@withContext Result.success()
                } else {
                    Timber.d("erro ao recuperar customId")
                    return@withContext Result.failure()
                }
            } catch (e: Exception) {
                Timber.d("ocorreu um problema no worker. \nDetalhes: $e")
                // this result is ignored in case of cancelling
                return@withContext Result.failure()
            }
        }
    }
}