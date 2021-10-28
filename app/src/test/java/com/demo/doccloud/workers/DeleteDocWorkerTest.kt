package com.demo.doccloud.workers

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.*
import androidx.work.testing.TestListenableWorkerBuilder
import com.demo.doccloud.domain.entities.Photo
import com.demo.doccloud.domain.usecases.contracts.DeleteRemoteDoc
import com.demo.doccloud.utils.AppConstants
import com.google.gson.Gson
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [30])
@RunWith(RobolectricTestRunner::class)
class DeleteDocWorkerTest {

    private lateinit var context: Context
    private val dispatcher = Dispatchers.IO
    private val deleteRemoteDocUseCase: DeleteRemoteDoc = mockk()

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    private fun getWorker(data: Data? = null): DeleteDocWorker{
        val worker =  TestListenableWorkerBuilder<DeleteDocWorker>(context)
            .setWorkerFactory(
                DeleteDocWorkerFactory()
            )

        data?.let {
            worker.setInputData(it)
        }
        return worker.build()
    }

    @Test
    fun deleteDocPageSuccessfully() {
        //Arrange
        val data = workDataOf(
            AppConstants.REMOTE_ID_KEY to 10L,
            AppConstants.JSON_PAGES_KEY to Gson().toJson(listOf(Photo(id=1, path = "dummyPath"))),
        )
        coEvery { deleteRemoteDocUseCase(any(), any()) } returns Unit
        val worker = getWorker(data)

        runBlocking {
            //Act
            val result = worker.doWork()

            //Assert
            MatcherAssert.assertThat(result, Matchers.`is`(ListenableWorker.Result.success()))
        }
    }

    @Test
    fun failureByRemoteIdNotFound(){
        //Arrange
        val data = workDataOf(
            AppConstants.JSON_PAGES_KEY to Gson().toJson(listOf(Photo(id=1, path = "dummyPath"))),
        )
        val worker = getWorker(data)

        runBlocking {
            //Act
            val result = worker.doWork()

            //Assert
            MatcherAssert.assertThat(result, Matchers.`is`(ListenableWorker.Result.failure()))
        }
    }

    @Test
    fun failureByPagesNotFound(){
        //Arrange
        val data = workDataOf(
            AppConstants.REMOTE_ID_KEY to 10L
        )
        val worker = getWorker(data)

        runBlocking {
            //Act
            val result = worker.doWork()

            //Assert
            MatcherAssert.assertThat(result, Matchers.`is`(ListenableWorker.Result.failure()))
        }
    }

    @Test
    fun failureByException() {
        //Arrange
        val data = workDataOf(
            AppConstants.REMOTE_ID_KEY to 10L,
            AppConstants.JSON_PAGES_KEY to Gson().toJson(listOf(Photo(id=1, path = "dummyPath"))),
        )
        coEvery { deleteRemoteDocUseCase(any(), any()) } throws RuntimeException()
        val worker = getWorker(data)

        runBlocking {
            //Act
            val result = worker.doWork()

            //Assert
            MatcherAssert.assertThat(result, Matchers.`is`(ListenableWorker.Result.failure()))
        }
    }
    private inner class DeleteDocWorkerFactory: WorkerFactory() {
        override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters): ListenableWorker {
            return DeleteDocWorker(
                appContext,
                workerParameters,
                dispatcher,
                deleteRemoteDocUseCase
            )
        }
    }
}