package com.demo.doccloud.workers

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.demo.doccloud.domain.entities.SyncStrategy
import com.demo.doccloud.domain.usecases.contracts.*
import com.demo.doccloud.utils.AppConstants
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.lang.RuntimeException

@Config(sdk = [30])
@RunWith(RobolectricTestRunner::class)
class SyncDataWorkerTest {

    private lateinit var context: Context
    private var getSavedCustomIdSyncStrategy: GetSavedCustomIdSyncStrategy = mockk()
    private var getSyncStrategy = mockk<GetSyncStrategy>()
    private var saveCustomIdSyncStrategy = mockk<SaveCustomIdSyncStrategy>()
    private var syncDataUseCase = mockk<SyncData>()
    private val dispatcher = Dispatchers.IO

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }
    @After
    fun teardown(){
        clearMocks(
            getSavedCustomIdSyncStrategy,
            getSyncStrategy,
            saveCustomIdSyncStrategy,
            syncDataUseCase
        )
    }


    private fun getWorker(): SyncDataWorker {
        return TestListenableWorkerBuilder<SyncDataWorker>(context)
            .setWorkerFactory(SyncDataWorkerFactory())
            .build()
    }

    @Test
    fun noSync() {
        //Arrange
        val oneWeekInTimestamp = 2629743L
        coEvery { getSyncStrategy() } returns SyncStrategy(expiration = oneWeekInTimestamp, lastUpdated = System.currentTimeMillis(), customId = 1L)
        coEvery { getSavedCustomIdSyncStrategy() } returns 1L

        val worker = getWorker()
        runBlocking {
            //Act
            val result = worker.doWork()

            //Assert
            MatcherAssert.assertThat(result, Matchers.`is`(ListenableWorker.Result.success()))
        }
    }

    @Test
    fun syncWithFirstCondition() {
        //Arrange
        val oneSecond = 1000L
        val lastWeek = System.currentTimeMillis() - 2629743L
        coEvery { getSyncStrategy() } returns SyncStrategy(expiration = oneSecond, lastUpdated = lastWeek, customId = 1L)
        coEvery { getSavedCustomIdSyncStrategy() } returns 1L
        coEvery { syncDataUseCase(any()) } returns Unit

        val worker = getWorker()

        runBlocking {
            //Act
            val result = worker.doWork()

            //Assert
            MatcherAssert.assertThat(result, Matchers.`is`(ListenableWorker.Result.success()))
        }
    }

    @Test
    fun syncWithSecondCondition() {
        //Arrange
        val oneWeekInTimestamp = 2629743L
        coEvery { getSyncStrategy() } returns SyncStrategy(expiration = oneWeekInTimestamp, lastUpdated = System.currentTimeMillis(), customId = 2L)
        coEvery { getSavedCustomIdSyncStrategy() } returns 1L
        coEvery { syncDataUseCase(any()) } returns Unit

        val worker = getWorker()
        runBlocking {
            //Act
            val result = worker.doWork()

            //Assert
            MatcherAssert.assertThat(result, Matchers.`is`(ListenableWorker.Result.success()))
        }
    }

    @Test
    fun failureWithException(){
        coEvery {  getSyncStrategy() } throws RuntimeException()

        val worker = getWorker()
        runBlocking {
            //Act
            val result = worker.doWork()

            //Assert
            MatcherAssert.assertThat(result, Matchers.`is`(ListenableWorker.Result.failure()))
        }

    }

    private inner class SyncDataWorkerFactory() : WorkerFactory() {
        override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters): ListenableWorker {
            return SyncDataWorker(
                appContext,
                workerParameters,
                dispatcher,
                getSavedCustomIdSyncStrategy,
                getSyncStrategy,
                saveCustomIdSyncStrategy,
                syncDataUseCase
            )
        }
    }
}