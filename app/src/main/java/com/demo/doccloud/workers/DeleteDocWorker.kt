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
import java.lang.Exception

@HiltWorker
class DeleteDocWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
    private val remoteDataSource: RemoteDataSource,
) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(dispatcher) {
            //AppConstants.JSON_PAGES_KEY to jsonPages,
            // Retrieve doc data
            val remoteId: Long = inputData.getLong(AppConstants.REMOTE_ID_KEY, -1L)
            val jsonPages = inputData.getString(AppConstants.JSON_PAGES_KEY) ?: ""
            try {
                if (remoteId != -1L && jsonPages != "") {
                    //could be Array<String>
                    val pages = Gson().fromJson(jsonPages, Array<Photo>::class.java).toList()
                    remoteDataSource.deleteDocFirebase(remoteId, pages)
                    return@withContext Result.success()
                }else{
                    Timber.d("Falha ao recuperar dados para exclusão na nuvem")
                    return@withContext Result.failure()
                }

            }catch (e: Exception){
                Timber.d("ocorreu um problema no worker. Detalhes: $e")
                // this result is ignored in case of cancelling
                return@withContext Result.failure()
            }
        }
    }
}