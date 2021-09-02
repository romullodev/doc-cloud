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
import com.demo.doccloud.utils.Result
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber

@HiltWorker
class AddDocPhotosWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource,
) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(dispatcher) {
            val localId: Long = inputData.getLong(AppConstants.LOCAL_ID_KEY, -1L)
            val json: String = inputData.getString(AppConstants.LIST_PHOTO_ADD_KEY) ?: ""
            val photosId: List<String> = Gson().fromJson(json, Array<String>::class.java).toList()
            val doc = localDataSource.getDoc(localId)
            try {
                if (json != "" && localId != -1L) {
                    localDataSource.updateDoc(
                        doc.copy(status = DocStatus.SENDING)
                    )
                    val filteredPhotos = ArrayList<Photo>()
                    val newJsonPages = ArrayList<Long>()
                    doc.pages.map { photo ->
                        newJsonPages.add(photo.id)
                        photosId.forEach { id ->
                            if (photo.id == id.toLong()) {
                                filteredPhotos.add(photo)
                            }
                        }
                    }
                    val result = remoteDataSource.addPhotosDoc(
                        remoteId = doc.remoteId,
                        photos = filteredPhotos.toList(),
                        newJsonPages = Gson().toJson(newJsonPages)
                    )
                    when (result.status) {
                        com.demo.doccloud.utils.Result.Status.SUCCESS -> {
                            localDataSource.updateDoc(
                                doc.copy(status = DocStatus.SENT)
                            )
                            return@withContext Result.success()
                        }
                        com.demo.doccloud.utils.Result.Status.ERROR -> {
                            Timber.d("Falha ao adicionar fotos no servidor")
                            localDataSource.updateDoc(
                                doc.copy(status = DocStatus.NOT_SENT)
                            )
                            return@withContext Result.failure()
                        }
                    }
                } else {
                    Timber.d("falha ao recuperar a lista ou localId")
                    return@withContext Result.failure()
                }

            } catch (e: Exception) {
                Timber.d("falha ao recuperar a lista ou localId")
                localDataSource.updateDoc(
                    doc.copy(status = DocStatus.NOT_SENT)
                )
                return@withContext Result.failure()
            }
        }
    }
}