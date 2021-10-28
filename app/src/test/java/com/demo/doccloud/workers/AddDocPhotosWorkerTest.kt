package com.demo.doccloud.workers

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.*
import androidx.work.testing.TestListenableWorkerBuilder
import com.demo.doccloud.FakeRepository
import com.demo.doccloud.domain.usecases.contracts.AddPhotoToRemoteDoc
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
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@Config(sdk = [30])
@RunWith(RobolectricTestRunner::class)
class AddDocPhotosWorkerTest {

    private lateinit var context: Context
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
    private val updateLocalDocUseCase: UpdateLocalDoc = mockk()
    private val getDocByIdUseCase: GetDocById = mockk()
    private val addPhotoToRemoteDocUseCase: AddPhotoToRemoteDoc = mockk()

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @After
    fun teardown() {
        clearMocks(
            updateLocalDocUseCase,
            getDocByIdUseCase,
            addPhotoToRemoteDocUseCase
        )
    }

    private fun getWorker(data: Data? = null): AddDocPhotosWorker{
        val worker = TestListenableWorkerBuilder<AddDocPhotosWorker>(context)
            .setWorkerFactory(
                AddDocPhotosWorkerFactory()
            )
        data?.let {
            worker.setInputData(it)
        }
        return worker.build()
    }

    @Test
    fun addDocPhotosSuccessfully() {
        //Arrange
        val data = workDataOf(
            AppConstants.LOCAL_ID_KEY to 10L,
            AppConstants.LIST_PHOTO_ADD_KEY to Gson().toJson(listOf(1L, 2L)),
        )
        coEvery { getDocByIdUseCase(any()) } returns FakeRepository.fakeDoc
        coEvery { updateLocalDocUseCase(any()) } returns Unit
        coEvery { addPhotoToRemoteDocUseCase(any(), any(), any()) } returns Unit

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
            AppConstants.LIST_PHOTO_ADD_KEY to Gson().toJson(listOf(1L, 2L)),
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
    fun failureByEmptyJson() {
        //Arrange
        val data = workDataOf(
            AppConstants.LOCAL_ID_KEY to 10L,
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
            AppConstants.LIST_PHOTO_ADD_KEY to Gson().toJson(listOf(1L, 2L)),
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

    private inner class AddDocPhotosWorkerFactory : WorkerFactory() {
        override fun createWorker(
            appContext: Context,
            workerClassName: String,
            workerParameters: WorkerParameters
        ): ListenableWorker {
            return AddDocPhotosWorker(
                appContext,
                workerParameters,
                dispatcher,
                updateLocalDocUseCase,
                getDocByIdUseCase,
                addPhotoToRemoteDocUseCase
            )
        }
    }
}