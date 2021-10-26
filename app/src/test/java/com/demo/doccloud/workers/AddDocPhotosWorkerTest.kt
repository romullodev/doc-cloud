package com.demo.doccloud.workers

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.demo.doccloud.FakeRepository
import com.demo.doccloud.domain.usecases.contracts.AddPhotoToRemoteDoc
import com.demo.doccloud.domain.usecases.contracts.GetDocById
import com.demo.doccloud.domain.usecases.contracts.UpdateLocalDoc
import com.demo.doccloud.utils.AppConstants
import com.google.gson.Gson
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@Config(sdk = [30])
@RunWith(RobolectricTestRunner::class)
class AddDocPhotosWorkerTest{

    private lateinit var context: Context

    @Before
    fun setup(){
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun addDocPhotosSuccessfully(){
        //Arrange
        val data = workDataOf(
            AppConstants.LOCAL_ID_KEY to 10L,
            AppConstants.LIST_PHOTO_ADD_KEY to Gson().toJson(listOf(1L, 2L)),
        )
        val getDocById = mockk<GetDocById>()
        val updateLocalDoc = mockk<UpdateLocalDoc>()
        val addPhotoToRemoteDoc = mockk<AddPhotoToRemoteDoc>()

        coEvery { getDocById(any()) } returns FakeRepository.fakeDoc
        coEvery { updateLocalDoc(any()) } returns Unit
        coEvery { addPhotoToRemoteDoc(any(), any(), any()) } returns Unit

        val worker =
            TestListenableWorkerBuilder<AddDocPhotosWorker>(context)
                .setWorkerFactory(AddDocPhotosWorkerFactory(
                    dispatcher = Dispatchers.Default,
                    updateLocalDocUseCase = updateLocalDoc,
                    getDocByIdUseCase = getDocById,
                    addPhotoToRemoteDocUseCase = addPhotoToRemoteDoc
                ))
                .setInputData(data)
                .build()

        runBlocking {
            //Act
            val result = worker.doWork()

            //Assert
            assert(result == ListenableWorker.Result.success())
        }
    }

    @Test
    fun failureByLocalIdNotFound(){
        //Arrange
        val data = workDataOf(
            AppConstants.LIST_PHOTO_ADD_KEY to Gson().toJson(listOf(1L, 2L)),
        )
        val getDocById = mockk<GetDocById>()
        coEvery { getDocById(any()) } returns FakeRepository.fakeDoc
        val worker =
            TestListenableWorkerBuilder<AddDocPhotosWorker>(context)
                .setWorkerFactory(AddDocPhotosWorkerFactory(
                    dispatcher = Dispatchers.Default,
                    updateLocalDocUseCase = mockk(),
                    getDocByIdUseCase = getDocById,
                    addPhotoToRemoteDocUseCase = mockk()
                ))
                .setInputData(data)
                .build()

        runBlocking {
            //Act
            val result = worker.doWork()

            //Assert
            assert(result == ListenableWorker.Result.failure())
        }
    }

    @Test
    fun failureByEmptyJson(){
        //Arrange
        val data = workDataOf(
            AppConstants.LOCAL_ID_KEY to 10L,
        )
        val getDocById = mockk<GetDocById>()
        coEvery { getDocById(any()) } returns FakeRepository.fakeDoc
        val worker =
            TestListenableWorkerBuilder<AddDocPhotosWorker>(context)
                .setWorkerFactory(AddDocPhotosWorkerFactory(
                    dispatcher = Dispatchers.Default,
                    updateLocalDocUseCase = mockk(),
                    getDocByIdUseCase = getDocById,
                    addPhotoToRemoteDocUseCase = mockk()
                ))
                .setInputData(data)
                .build()

        runBlocking {
            //Act
            val result = worker.doWork()

            //Assert
            assert(result == ListenableWorker.Result.failure())
        }
    }

    @Test
    fun failureByException(){
        //Arrange
        val data = workDataOf(
            AppConstants.LOCAL_ID_KEY to 10L,
            AppConstants.LIST_PHOTO_ADD_KEY to Gson().toJson(listOf(1L, 2L)),
        )
        val getDocById = mockk<GetDocById>()

        coEvery { getDocById(any()) } throws RuntimeException()


        val worker =
            TestListenableWorkerBuilder<AddDocPhotosWorker>(context)
                .setWorkerFactory(AddDocPhotosWorkerFactory(
                    dispatcher = Dispatchers.Default,
                    updateLocalDocUseCase = mockk(),
                    getDocByIdUseCase = getDocById,
                    addPhotoToRemoteDocUseCase = mockk()
                ))
                .setInputData(data)
                .build()

        runBlocking {
            //Act
            val result = worker.doWork()

            //Assert
            assert(result == ListenableWorker.Result.failure())
        }
    }
}

class AddDocPhotosWorkerFactory(
    private val dispatcher: CoroutineDispatcher,
    private val updateLocalDocUseCase: UpdateLocalDoc,
    private val getDocByIdUseCase: GetDocById,
    private val addPhotoToRemoteDocUseCase : AddPhotoToRemoteDoc
) : WorkerFactory() {
    override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters): ListenableWorker {
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