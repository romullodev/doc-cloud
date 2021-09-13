package com.demo.doccloud.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.demo.doccloud.R
import com.demo.doccloud.data.datasource.local.LocalDataSource
import com.demo.doccloud.data.datasource.remote.RemoteDataSource
import com.demo.doccloud.di.IoDispatcher
import com.demo.doccloud.domain.entities.DocStatus
import com.demo.doccloud.domain.entities.Photo
import com.demo.doccloud.domain.usecases.contracts.AddPhotoToRemoteDoc
import com.demo.doccloud.domain.usecases.contracts.GetDocById
import com.demo.doccloud.domain.usecases.contracts.UpdateLocalDoc
import com.demo.doccloud.utils.AppConstants
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
    private val updateLocalDocUseCase: UpdateLocalDoc,
    private val getDocByIdUseCase: GetDocById,
    private val addPhotoToRemoteDocUseCase : AddPhotoToRemoteDoc
) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(dispatcher) {
            val localId: Long = inputData.getLong(AppConstants.LOCAL_ID_KEY, -1L)
            val json: String = inputData.getString(AppConstants.LIST_PHOTO_ADD_KEY) ?: ""
            val photosId: List<String> = Gson().fromJson(json, Array<String>::class.java).toList()
            val doc = getDocByIdUseCase(localId)
            try {
                if (json != "" && localId != -1L) {
                    updateLocalDocUseCase(doc.copy(status = DocStatus.SENDING))
                    val filteredPhotos = ArrayList<Photo>()
                    val newJsonPages = ArrayList<Long>()
                    //could be improved with filter
                    doc.pages.map { photo ->
                        newJsonPages.add(photo.id)
                        photosId.forEach { id ->
                            if (photo.id == id.toLong()) {
                                filteredPhotos.add(photo)
                            }
                        }
                    }
                    addPhotoToRemoteDocUseCase(
                        remoteId = doc.remoteId,
                        photos = filteredPhotos.toList(),
                        newJsonPages = Gson().toJson(newJsonPages)
                    )
                    return@withContext Result.success()
                } else {
                    Timber.d(applicationContext.getString(R.string.work_manager_failure_recover_list_or_id))
                    return@withContext Result.failure()
                }

            } catch (e: Exception) {
                Timber.d(applicationContext.getString(R.string.work_manager_failure_recover_list_or_id))
                updateLocalDocUseCase(doc.copy(status = DocStatus.NOT_SENT))
                return@withContext Result.failure()
            }
        }
    }
}