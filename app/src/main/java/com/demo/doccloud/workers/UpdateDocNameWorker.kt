package com.demo.doccloud.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.demo.doccloud.data.datasource.local.LocalDataSource
import com.demo.doccloud.data.datasource.remote.RemoteDataSource
import com.demo.doccloud.di.IoDispatcher
import com.demo.doccloud.domain.entities.DocStatus
import com.demo.doccloud.domain.usecases.contracts.GetDocById
import com.demo.doccloud.domain.usecases.contracts.UpdateLocalDoc
import com.demo.doccloud.domain.usecases.contracts.UpdateRemoteDocName
import com.demo.doccloud.utils.AppConstants
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber

@HiltWorker
class UpdateDocNameWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
    private val updateLocalDoc: UpdateLocalDoc,
    private val updateRemoteDocNameUseCase: UpdateRemoteDocName,
    private val getDocByIdUseCase: GetDocById
) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(dispatcher) {
            // Retrieve doc info
            val localId: Long = inputData.getLong(AppConstants.LOCAL_ID_KEY, -1L)
            val remoteId: Long = inputData.getLong(AppConstants.REMOTE_ID_KEY, -1L)
            val name: String = inputData.getString(AppConstants.DOC_NAME_ID_KEY) ?: ""
            val doc = getDocByIdUseCase(localId)
            try {
                //in case of deletion
                if (remoteId != -1L && name != "") {
                    updateLocalDoc(doc.copy(status = DocStatus.SENDING))
                    //upload to server
                    updateRemoteDocNameUseCase(
                        remoteId = doc.remoteId,
                        name = name
                    )
                    updateLocalDoc(doc.copy(status = DocStatus.SENT))
                    return@withContext Result.success()
                } else {
                    Timber.d("erro ao recuperar informações do documento")
                    return@withContext Result.failure()
                }
            } catch (e: Exception) {
                Timber.d("ocorreu um problema no worker. \nDetalhes: $e")
                updateLocalDoc(doc.copy(status = DocStatus.NOT_SENT))
                // this result is ignored in case of cancelling
                return@withContext Result.failure()
            }
        }
    }
}