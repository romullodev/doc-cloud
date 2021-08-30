package com.demo.doccloud.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.demo.doccloud.data.datasource.local.LocalDataSource
import com.demo.doccloud.data.datasource.remote.RemoteDataSource
import com.demo.doccloud.di.IoDispatcher
import com.demo.doccloud.domain.DocStatus
import com.demo.doccloud.domain.Photo
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
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource,
) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(dispatcher) {
            // Retrieve localId
            val localId: Long = inputData.getLong(AppConstants.LOCAL_ID_KEY, -1L)
            val photoId: Long = inputData.getLong(AppConstants.PHOTO_ID_KEY, -1L)
            val photoPath: String = inputData.getString(AppConstants.PHOTO_PATH_KEY) ?: ""

            val doc = localDataSource.getDoc(localId)
            try {
                //in case of deletion
                if (localId != -1L && photoId != -1L && photoPath != "") {
                    localDataSource.updateDoc(
                        doc.copy(status = DocStatus.SENDING)
                    )
                    //upload to server
                    val result = remoteDataSource.updateDocPhotosFirebase(
                        remoteId = localDataSource.getDoc(localId).remoteId,
                        photo = Photo(id = photoId, path = photoPath)
                    )
                    when(result.status){
                        com.demo.doccloud.utils.Result.Status.SUCCESS -> {
                            localDataSource.updateDoc(
                                doc.copy(status = DocStatus.SENT)
                            )
                            return@withContext Result.success()
                        }
                        com.demo.doccloud.utils.Result.Status.ERROR -> {
                            Timber.d("ocorreu um problema ao atualizar a página do documento. \nDetalhes: ${result.msg}")
                            localDataSource.updateDoc(
                                doc.copy(status = DocStatus.NOT_SENT)
                            )
                            return@withContext Result.failure()
                        }
                    }
                } else {
                    Timber.d("erro ao recuperar informações do documento")
                    localDataSource.updateDoc(
                        doc.copy(status = DocStatus.NOT_SENT)
                    )
                    return@withContext Result.failure()
                }
            } catch (e: Exception) {
                Timber.d("ocorreu um problema no worker. \nDetalhes: $e")
                localDataSource.updateDoc(
                    doc.copy(status = DocStatus.NOT_SENT)
                )
                // this result is ignored in case of cancelling
                return@withContext Result.failure()
            }
        }
    }
}