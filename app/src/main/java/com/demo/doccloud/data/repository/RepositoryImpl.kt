package com.demo.doccloud.data.repository

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.work.*
import com.demo.doccloud.R
import com.demo.doccloud.data.datasource.local.LocalDataSource
import com.demo.doccloud.data.datasource.local.room.entities.DatabaseDoc
import com.demo.doccloud.data.datasource.remote.RemoteDataSource
import com.demo.doccloud.domain.Doc
import com.demo.doccloud.domain.DocStatus
import com.demo.doccloud.domain.Photo
import com.demo.doccloud.utils.AppConstants
import com.demo.doccloud.utils.Result
import com.demo.doccloud.workers.DeleteDocWorker
import com.demo.doccloud.workers.UpdateDocNameWorker
import com.demo.doccloud.workers.UpdateDocPageWorker
import com.demo.doccloud.workers.UploadDocWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class RepositoryImpl @Inject constructor(
    private val remoteDatasource: RemoteDataSource,
    private val localDatasource: LocalDataSource,
    @ApplicationContext private val context: Context,
) : Repository {
    override val docs: LiveData<List<Doc>> get() = localDatasource.getSavedDocs()

    override suspend fun doLoginWithGoogle(data: Intent?) = remoteDatasource.doLoginWithGoogle(data)

    override suspend fun getUser() = remoteDatasource.getUser()

    override suspend fun doLogout() = remoteDatasource.doLogout()

    override suspend fun saveDoc(doc: Doc): Result<Boolean> {
        val rowNumber: Long = localDatasource.saveDocOnDevice(doc)
        //schedule to send docs to server
        setupSendDocSchedule(rowNumber)
        return Result.success(true)
    }

    override suspend fun deleteDoc(doc: Doc): Result<String> {
        localDatasource.deleteDocOnDevice(doc)
        //schedule to delete docs from server
        setupDeleteDocSchedule(doc.remoteId, doc.pages.size)
        return Result.success(context.getString(R.string.home_toast_delete_success, doc.name))
    }

    override suspend fun getDoc(id: Long): Result<Doc> {
        val doc =  localDatasource.getDoc(id)
        return Result.success(doc)
    }

    override suspend fun updateDocName(localId: Long, remoteId: Long, name: String) {
        localDatasource.updateDocName(localId, name)
        val doc = localDatasource.getDoc(localId)
        localDatasource.updateDoc(
            doc.copy(status = DocStatus.NOT_SENT)
        )
        setupUpdateDocNameSchedule(localId = localId, remoteId = remoteId, name = name)
    }

    override suspend fun updateDocPhotos(localId: Long, remoteId: Long, photo: Photo) {
        localDatasource.updateDocPhoto(localId = localId, photo = photo)
        val doc = localDatasource.getDoc(localId)
        localDatasource.updateDoc(
            doc.copy(status = DocStatus.NOT_SENT)
        )
        setupUpdateDocPhotosSchedule(localId = localId, photo = photo)
    }


    private fun setupUpdateDocNameSchedule(localId: Long, remoteId: Long, name: String) {
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

    private fun setupUpdateDocPhotosSchedule(localId: Long, photo: Photo) {
        val data =
            workDataOf(
                AppConstants.LOCAL_ID_KEY to localId,
                AppConstants.PHOTO_ID_KEY to photo.id,
                AppConstants.PHOTO_PATH_KEY to photo.path,
                )

        //setup constraint to workManager (only send if network is available)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        //setup the request work to send
        val updateDocNameWorkRequest =
            OneTimeWorkRequestBuilder<UpdateDocPageWorker>()
                .setInputData(data)
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .addTag("${localId}_${photo.id}")
                .build()
        //cancel a schedule work in case it has been previous setup by this same localId + photo tag
        //this code could be improved
        WorkManager.getInstance(context).cancelAllWorkByTag("${localId}_${photo.id}")

        //schedule the work to be done
        WorkManager.getInstance(context).enqueue(updateDocNameWorkRequest)
    }

    private fun setupSendDocSchedule(rowNumber: Long) {
        val data =
            workDataOf(
                AppConstants.LOCAL_ID_KEY to rowNumber,
            )

        //setup constraint to workManager (only send if network is available)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        //setup the request work to send
        val uploadWorkRequest =
            OneTimeWorkRequestBuilder<UploadDocWorker>()
                .setInputData(data)
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

        //schedule the work to be done
        WorkManager.getInstance(context).enqueue(uploadWorkRequest)
    }

    private fun setupDeleteDocSchedule(remoteId: Long, pagesNumber: Int){
        val data =
            workDataOf(
                AppConstants.REMOTE_ID_KEY to remoteId,
                AppConstants.PAGES_NUMBER_KEY to pagesNumber,
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