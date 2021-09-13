package com.demo.doccloud.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.demo.doccloud.di.IoDispatcher
import com.demo.doccloud.domain.entities.DocStatus
import com.demo.doccloud.domain.entities.Photo
import com.demo.doccloud.domain.usecases.contracts.GetDocById
import com.demo.doccloud.domain.usecases.contracts.UpdateLocalDoc
import com.demo.doccloud.domain.usecases.contracts.UpdateRemoteDocPhotos
import com.demo.doccloud.utils.AppConstants
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber

@HiltWorker
class UpdateDocPageWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
    private val getDocById: GetDocById,
    private val updateLocalDocUseCase: UpdateLocalDoc,
    private val updateRemoteDocPhotosUseCase: UpdateRemoteDocPhotos
) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(dispatcher) {
            // Retrieve localId
            val localId: Long = inputData.getLong(AppConstants.LOCAL_ID_KEY, -1L)
            val photoId: Long = inputData.getLong(AppConstants.PHOTO_ID_KEY, -1L)
            val photoPath: String = inputData.getString(AppConstants.PHOTO_PATH_KEY) ?: ""

            val doc = getDocById(localId)
            try {
                //in case of deletion
                if (localId != -1L && photoId != -1L && photoPath != "") {
                    updateLocalDocUseCase(doc.copy(status = DocStatus.SENDING))
                    //upload to server
                    updateRemoteDocPhotosUseCase(
                        remoteId = doc.remoteId,
                        photo = Photo(id = photoId, path = photoPath)
                    )
                    updateLocalDocUseCase(doc.copy(status = DocStatus.SENT))
                    return@withContext Result.success()
                } else {
                    Timber.d("erro ao recuperar informações do documento")
                    return@withContext Result.failure()
                }
            } catch (e: Exception) {
                Timber.d("ocorreu um problema no worker. \nDetalhes: $e")
                updateLocalDocUseCase(doc.copy(status = DocStatus.NOT_SENT))
                // this result is ignored in case of cancelling
                return@withContext Result.failure()
            }
        }
    }
}