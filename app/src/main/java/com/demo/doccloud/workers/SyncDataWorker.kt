package com.demo.doccloud.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.lifecycle.MutableLiveData
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.demo.doccloud.data.datasource.local.LocalDataSource
import com.demo.doccloud.data.datasource.remote.RemoteDataSource
import com.demo.doccloud.di.IoDispatcher
import com.demo.doccloud.domain.SyncStrategy
import com.demo.doccloud.utils.AppConstants.Companion.DATABASE_DEFAULT_CUSTOM_ID
import com.demo.doccloud.utils.Result
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
            //Before schedule, it checks if this sync is indeed necessary be retrieving sync strategy model
            val resultSync = remoteDataSource.getSyncStrategy()
            when(resultSync.status){
                com.demo.doccloud.utils.Result.Status.SUCCESS -> {
                    val syncStrategy : SyncStrategy = resultSync.data!!
                    val customID: Long = localDataSource.getSavedCustomId()
                    if(
                        syncStrategy.lastUpdated + syncStrategy.expiration <= System.currentTimeMillis() || // (2): user has passed much time with no synced data
                        syncStrategy.customId != customID // (3): user do login in another device OR local service failure on get custom ID saves on the device
                    ){
                        val result  = if(syncStrategy.customId == DATABASE_DEFAULT_CUSTOM_ID){
                            val saveCustomIdResult = localDataSource.saveCustomId()
                            when(saveCustomIdResult.status){
                                com.demo.doccloud.utils.Result.Status.SUCCESS -> {
                                    syncDataProgress.postValue(0L)
                                    remoteDataSource.syncData(saveCustomIdResult.data!!)
                                }
                                com.demo.doccloud.utils.Result.Status.ERROR -> {
                                    Timber.d("failure on save custom ID on the device.")
                                    return@withContext Result.failure()
                                }
                            }
                        }else{
                            syncDataProgress.postValue(0L)
                            remoteDataSource.syncData(customID)
                        }
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

                    }else{
                        Timber.d("no need sync")
                        Result.success()
                    }
                }
                com.demo.doccloud.utils.Result.Status.ERROR -> {
                    Timber.d("Error on get sync strategy. \nDetails: ${resultSync.msg}")
                    Result.failure()
                }
            }
        }
    }


}