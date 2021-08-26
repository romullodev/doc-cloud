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
import com.demo.doccloud.utils.AppConstants
import com.demo.doccloud.utils.Result
import com.demo.doccloud.workers.DeleteDocWorker
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