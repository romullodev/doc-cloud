package com.demo.doccloud.workers

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.work.*
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import com.demo.doccloud.domain.usecases.contracts.RemoveTempFile
import com.demo.doccloud.utils.AppConstants
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit

@Config(sdk = [30])
@RunWith(RobolectricTestRunner::class)
class RemoveTempFileWorkerTest {

    private lateinit var context: Context
    private val dispatcher = Dispatchers.IO
    private val removeTempFileUseCase = mockk<RemoveTempFile>()

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @After
    fun teardown(){
        clearMocks(removeTempFileUseCase)
    }

    private fun getBuildWorker(data: Data? = null): RemoveTempFileWorker{
        val worker = TestListenableWorkerBuilder<RemoveTempFileWorker>(context)
            .setWorkerFactory(
                RemoveTempFileWorkerFactory()
            )
        data?.let {
            worker.setInputData(it)
        }
        return worker.build()
    }

    @Test
    fun checkInitialDelay() {
        coEvery { removeTempFileUseCase(any()) } returns Unit
        val config = Configuration.Builder()
            .setWorkerFactory(
                RemoveTempFileWorkerFactory()
            )
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()
        // Initialize WorkManager
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)

        //setup the request work to send
        val request =
            OneTimeWorkRequestBuilder<RemoveTempFileWorker>()
                .setInitialDelay(10, TimeUnit.MINUTES)
                .setInputData(
                    workDataOf(
                        AppConstants.CUSTOM_ID_KEY to 10L
                    )
                )
                .build()

        val workManager = WorkManager.getInstance(context)
        // Get the TestDriver
        val testDriver = WorkManagerTestInitHelper.getTestDriver(context)
        // Enqueue
        workManager.enqueue(request)
        // Tells the WorkManager test framework that initial delays are now met.
        testDriver?.setInitialDelayMet(request.id)
        // Get WorkInfo and outputData
        val workInfo = workManager.getWorkInfoById(request.id).get()
        // Assert
        assertThat(workInfo.state, `is`(WorkInfo.State.SUCCEEDED))
    }

    @Test
    fun removeTempFileSuccessfully() {
        //Arrange
        coEvery { removeTempFileUseCase(any()) } returns Unit
        val worker = getBuildWorker(
            workDataOf(
                AppConstants.CUSTOM_ID_KEY to 10L
            )
        )
        runBlocking {
            //Act
            val result = worker.doWork()

            //Assert
            MatcherAssert.assertThat(result, Matchers.`is`(ListenableWorker.Result.success()))
        }
    }

    @Test
    fun failureWithCustomIdNotFound() {
        //Arrange
        val worker = getBuildWorker(null)

        runBlocking {
            //Act
            val result = worker.doWork()

            //Assert
            assertThat(result, Matchers.`is`(ListenableWorker.Result.failure()))
        }
    }

    @Test
    fun failureWithException() {
        //Arrange
        val data = workDataOf(
            AppConstants.CUSTOM_ID_KEY to 10L
        )
        coEvery { removeTempFileUseCase(any()) } throws RuntimeException()
        val worker = getBuildWorker(data)

        runBlocking {
            //Act
            val result = worker.doWork()

            //Assert
            MatcherAssert.assertThat(result, Matchers.`is`(ListenableWorker.Result.failure()))
        }
    }

    private inner class RemoveTempFileWorkerFactory : WorkerFactory() {
        override fun createWorker(
            appContext: Context,
            workerClassName: String,
            workerParameters: WorkerParameters
        ): ListenableWorker {
            return RemoveTempFileWorker(
                appContext,
                workerParameters,
                dispatcher,
                removeTempFileUseCase
            )
        }
    }
}