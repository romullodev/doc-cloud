package com.demo.doccloud.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.lifecycle.MutableLiveData
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.demo.doccloud.di.IoDispatcher
import com.demo.doccloud.domain.usecases.contracts.GetSavedCustomIdSyncStrategy
import com.demo.doccloud.domain.usecases.contracts.GetSyncStrategy
import com.demo.doccloud.domain.usecases.contracts.SaveCustomIdSyncStrategy
import com.demo.doccloud.domain.usecases.contracts.SyncData
import com.demo.doccloud.utils.AppConstants.Companion.DATABASE_DEFAULT_CUSTOM_ID
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
    private val getSavedCustomIdSyncStrategyUseCase: GetSavedCustomIdSyncStrategy,
    private val getSyncStrategyUseCase: GetSyncStrategy,
    private val saveCustomIdSyncStrategyUseCase: SaveCustomIdSyncStrategy,
    private val syncDataUseCase: SyncData
) :
    CoroutineWorker(appContext, workerParams) {

    companion object {
        val syncDataProgress = MutableLiveData(-1L)
    }

    override suspend fun doWork(): Result {
        return withContext(dispatcher) {
            try {
                //Before schedule, it checks if this sync is indeed necessary be retrieving sync strategy model
                val syncStrategy = getSyncStrategyUseCase()
                val customID: Long = getSavedCustomIdSyncStrategyUseCase()
                val firstCondition = syncStrategy.lastUpdated + syncStrategy.expiration <= System.currentTimeMillis() // (1): user has passed much time with no synced data
                val secondCondition = syncStrategy.customId != customID // (2): user do login in another device OR local service failure on get custom ID saves on the device
                if (firstCondition || secondCondition) {
                    val id: Long =
                        if (syncStrategy.customId == DATABASE_DEFAULT_CUSTOM_ID) saveCustomIdSyncStrategyUseCase() else customID
                    syncDataProgress.postValue(0L)
                    syncDataUseCase(id)
                    syncDataProgress.postValue(-1L)
                    return@withContext Result.success()
                } else {
                    Timber.d("no need sync")
                    Result.success()
                }
            } catch (e: Exception) {
                Timber.d("error on sync data. \nDetails: $e")
                return@withContext Result.failure()
            }
        }
    }
}
