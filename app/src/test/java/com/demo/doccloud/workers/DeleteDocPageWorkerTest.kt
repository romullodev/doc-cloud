package com.demo.doccloud.workers

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.*
import androidx.work.testing.TestListenableWorkerBuilder
import com.demo.doccloud.FakeRepository
import com.demo.doccloud.di.IoDispatcher
import com.demo.doccloud.domain.usecases.contracts.AddPhotoToRemoteDoc
import com.demo.doccloud.domain.usecases.contracts.DeleteRemoteDocPhoto
import com.demo.doccloud.domain.usecases.contracts.GetDocById
import com.demo.doccloud.domain.usecases.contracts.UpdateLocalDoc
import com.demo.doccloud.utils.AppConstants
import com.google.gson.Gson
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
class DeleteDocPageWorkerTest {

    private lateinit var context: Context
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
    private val updateLocalDocUseCase: UpdateLocalDoc = mockk()
    private val getDocByIdUseCase: GetDocById = mockk()
    private val deleteRemoteDocPhotoUseCase: DeleteRemoteDocPhoto = mockk()

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @After
    fun teardown() {
        clearMocks(
            updateLocalDocUseCase,
            getDocByIdUseCase,
            deleteRemoteDocPhotoUseCase
        )
    }

    private fun getWorker(data: Data? = null): DeleteDocPageWorker {
        val worker = TestListenableWorkerBuilder<DeleteDocPageWorker>(context)
            .setWorkerFactory(
                DeleteDocPageWorkerFactory()
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
            AppConstants.LOCAL_ID_KEY to 10L,
            AppConstants.PHOTO_ID_KEY to 10L,
            AppConstants.PHOTO_PATH_KEY to "dummyPath"
        )
        coEvery { getDocByIdUseCase(any()) } returns FakeRepository.fakeDoc
        coEvery { updateLocalDocUseCase(any()) } returns Unit
        coEvery { deleteRemoteDocPhotoUseCase(any(), any(), any()) } returns Unit
        val worker = getWorker(data)

            runBlocking {
                //Act
                val result = worker.doWork()

                //Assert
                MatcherAssert.assertThat(result, Matchers.`is`(ListenableWorker.Result.success()))
            }
    }

    @Test
    fun failureByLocalIdNotFound() {
        //Arrange
        val data = workDataOf(
            AppConstants.PHOTO_ID_KEY to 10L,
            AppConstants.PHOTO_PATH_KEY to "dummyPath"
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
    fun failureByPhotoIdNotFound() {
        //Arrange
        val data = workDataOf(
            AppConstants.LOCAL_ID_KEY to 10L,
            AppConstants.PHOTO_PATH_KEY to "dummyPath"
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
    fun failureByPhotoPathNotFound() {
        //Arrange
        val data = workDataOf(
            AppConstants.LOCAL_ID_KEY to 10L,
            AppConstants.PHOTO_ID_KEY to 10L
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
            AppConstants.LOCAL_ID_KEY to 10L,
            AppConstants.PHOTO_ID_KEY to 10L,
            AppConstants.PHOTO_PATH_KEY to "dummyPath"
        )

        coEvery { getDocByIdUseCase(any()) } throws RuntimeException()

        val worker = getWorker(data)

        runBlocking {
            //Act
            val result = worker.doWork()

            //Assert
            MatcherAssert.assertThat(result, Matchers.`is`(ListenableWorker.Result.failure()))
        }
    }

    private inner class DeleteDocPageWorkerFactory: WorkerFactory() {
        override fun createWorker(
            appContext: Context,
            workerClassName: String,
            workerParameters: WorkerParameters
        ): ListenableWorker {
            return DeleteDocPageWorker(
                appContext,
                workerParameters,
                dispatcher,
                updateLocalDocUseCase,
                getDocByIdUseCase,
                deleteRemoteDocPhotoUseCase
            )
        }
    }
}