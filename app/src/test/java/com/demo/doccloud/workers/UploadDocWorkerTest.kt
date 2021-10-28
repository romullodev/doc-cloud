package com.demo.doccloud.workers

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.*
import androidx.work.testing.TestListenableWorkerBuilder
import com.demo.doccloud.FakeRepository
import com.demo.doccloud.domain.usecases.contracts.GetDocById
import com.demo.doccloud.domain.usecases.contracts.UpdateLocalDoc
import com.demo.doccloud.domain.usecases.contracts.UploadDoc
import com.demo.doccloud.utils.AppConstants
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.mockk
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
class UploadDocWorkerTest {

    private lateinit var context: Context
    private val dispatcher = Dispatchers.IO
    private val getDocById: GetDocById = mockk()
    private val updateLocalDoc: UpdateLocalDoc = mockk()
    private val uploadDocUseCase: UploadDoc = mockk()

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @After
    fun teardown() {
        clearMocks(
            getDocById,
            updateLocalDoc,
            uploadDocUseCase
        )
    }

    private fun getWorker(data: Data? = null): UploadDocWorker {
        val worker = TestListenableWorkerBuilder<UploadDocWorker>(context)
            .setWorkerFactory(UploadDocWorkerFactory())

        data?.let {
            worker.setInputData(it)
        }
        return worker.build()
    }

    @Test
    fun uploadDocWorkerSuccessfully() {
        //Assert
        coEvery { getDocById(any()) } returns FakeRepository.fakeDoc
        coEvery { updateLocalDoc(any()) } returns Unit
        coEvery { uploadDocUseCase(any()) } returns Unit
        val data =
            workDataOf(
                AppConstants.LOCAL_ID_KEY to 10L
            )
        val worker = getWorker(data)
        runBlocking {
            //Act
            val result = worker.doWork()

            //Assert
            MatcherAssert.assertThat(result, Matchers.`is`(ListenableWorker.Result.success()))
        }
    }

    @Test
    fun failureWithException(){
        //Assert
        coEvery { getDocById(any()) } throws RuntimeException()
        val data =
            workDataOf(
                AppConstants.LOCAL_ID_KEY to 10L
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
    fun failureWithLocalIdNotFound(){
        //Assert
        val worker = getWorker()
        runBlocking {
            //Act
            val result = worker.doWork()

            //Assert
            MatcherAssert.assertThat(result, Matchers.`is`(ListenableWorker.Result.failure()))
        }

    }

    private inner class UploadDocWorkerFactory : WorkerFactory() {
        override fun createWorker(
            appContext: Context,
            workerClassName: String,
            workerParameters: WorkerParameters
        ): ListenableWorker {
            return UploadDocWorker(
                appContext,
                workerParameters,
                dispatcher,
                getDocById,
                updateLocalDoc,
                uploadDocUseCase
            )
        }
    }
}