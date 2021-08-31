package com.demo.doccloud.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.lifecycle.MutableLiveData
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.demo.doccloud.data.datasource.local.LocalDataSource
import com.demo.doccloud.data.datasource.remote.RemoteDataSource
import com.demo.doccloud.di.IoDispatcher
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber

@HiltWorker
class SyncDataWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource,
) :
    CoroutineWorker(appContext, workerParams) {

    companion object {
        val syncDataProgress = MutableLiveData(-1L)
    }

    override suspend fun doWork(): Result {
        return withContext(dispatcher) {
            syncDataProgress.postValue(0L)
            val result  = remoteDataSource.syncData()
            return@withContext when(result.status){
                com.demo.doccloud.utils.Result.Status.SUCCESS -> {
                    localDataSource.syncData(result.data!!)
                    syncDataProgress.postValue(-1L)
                    Result.success()
                }
                com.demo.doccloud.utils.Result.Status.ERROR -> {
                    syncDataProgress.postValue(-1L)
                    Timber.d("error on sync data. \nDetails: ${result.msg}")
                    Result.failure()
                }
            }
        }
    }
}