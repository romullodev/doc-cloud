package com.demo.doccloud.workers

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.*
import androidx.work.testing.TestListenableWorkerBuilder
import com.demo.doccloud.FakeRepository
import com.demo.doccloud.domain.usecases.contracts.GetDocById
import com.demo.doccloud.domain.usecases.contracts.UpdateLocalDoc
import com.demo.doccloud.domain.usecases.contracts.UpdateRemoteDocName
import com.demo.doccloud.domain.usecases.contracts.UpdateRemoteDocPhoto
import com.demo.doccloud.utils.AppConstants
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.mockk
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
class UpdateDocPageWorkerTest {

    private lateinit var context: Context
    private val dispatcher = Dispatchers.IO
    private val getDocById: GetDocById = mockk()
    private val updateLocalDocUseCase: UpdateLocalDoc = mockk()
    private val updateRemoteDocPhotosUseCase: UpdateRemoteDocPhoto = mockk()

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @After
    fun teardown() {
        clearMocks(
            getDocById,
            updateLocalDocUseCase,
            updateRemoteDocPhotosUseCase
        )
    }

    private fun getWorker(data: Data? = null): UpdateDocPageWorker {
        val worker = TestListenableWorkerBuilder<UpdateDocPageWorker>(context)
            .setWorkerFactory(UpdateDocPageWorkerFactory())

        data?.let {
            worker.setInputData(it)
        }
        return worker.build()
    }

    @Test
    fun failureWithLocalIdNotFound(){
        //Assert
        val data =
            workDataOf(
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
    fun failureWithPhotoIdNotFound(){
        //Assert
        val data =
            workDataOf(
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
    fun failureWithPhotoPathNotFound(){
        //Assert
        val data =
            workDataOf(
                AppConstants.LOCAL_ID_KEY to 10L,
                AppConstants.PHOTO_ID_KEY to 10L,
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
    fun failureWithException(){
        //Assert
        coEvery { getDocById(any()) } throws RuntimeException()
        val data =
            workDataOf(
                AppConstants.LOCAL_ID_KEY to 10L,
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
    fun updateDocPageSuccessfully() {
        //Assert
        coEvery { getDocById(any()) } returns FakeRepository.fakeDoc
        coEvery { updateLocalDocUseCase(any()) } returns Unit
        coEvery { updateRemoteDocPhotosUseCase(any(), any()) } returns Unit
        val data =
            workDataOf(
                AppConstants.LOCAL_ID_KEY to 10L,
                AppConstants.PHOTO_ID_KEY to 10L,
                AppConstants.PHOTO_PATH_KEY to "dummyPath"
            )
        val worker = getWorker(data)
        runBlocking {
            //Act
            val result = worker.doWork()

            //Assert
            MatcherAssert.assertThat(result, Matchers.`is`(ListenableWorker.Result.success()))
        }
    }

    private inner class UpdateDocPageWorkerFactory : WorkerFactory() {
        override fun createWorker(
            appContext: Context,
            workerClassName: String,
            workerParameters: WorkerParameters
        ): ListenableWorker {
            return UpdateDocPageWorker(
                appContext,
                workerParameters,
                dispatcher,
                getDocById,
                updateLocalDocUseCase,
                updateRemoteDocPhotosUseCase
            )
        }
    }
}