package com.demo.doccloud.workers

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.*
import androidx.work.testing.TestListenableWorkerBuilder
import com.demo.doccloud.FakeRepository
import com.demo.doccloud.domain.usecases.contracts.GetDocById
import com.demo.doccloud.domain.usecases.contracts.UpdateLocalDoc
import com.demo.doccloud.domain.usecases.contracts.UpdateRemoteDocName
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
class UpdateDocNameWorkerTest {

    private lateinit var context: Context
    private val dispatcher = Dispatchers.IO
    private val updateLocalDoc: UpdateLocalDoc = mockk()
    private val updateRemoteDocNameUseCase: UpdateRemoteDocName = mockk()
    private val getDocByIdUseCase: GetDocById = mockk()

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }
    @After
    fun teardown(){
        clearMocks(
            updateLocalDoc,
            updateRemoteDocNameUseCase,
            getDocByIdUseCase
        )
    }

    private fun getWorker(data: Data? = null): UpdateDocNameWorker {
        val worker =  TestListenableWorkerBuilder<UpdateDocNameWorker>(context)
            .setWorkerFactory(UpdateDocNameWorkerFactory())

        data?.let {
            worker.setInputData(it)
        }
        return worker.build()
    }

    @Test
    fun failureWithRemoteIdNotFound(){
        //Assert
        val data =
            workDataOf(
                AppConstants.LOCAL_ID_KEY to 10L,
                AppConstants.DOC_NAME_ID_KEY to "dummyName",
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
    fun failureWithNameNotFound(){
        //Assert
        val data =
            workDataOf(
                AppConstants.LOCAL_ID_KEY to 10L,
                AppConstants.REMOTE_ID_KEY to 10L,
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
        coEvery { getDocByIdUseCase(any()) } throws RuntimeException()
        val worker = getWorker()
        runBlocking {
            //Act
            val result = worker.doWork()

            //Assert
            MatcherAssert.assertThat(result, Matchers.`is`(ListenableWorker.Result.failure()))
        }
    }

    @Test
    fun updateDocNameSuccessfully(){
        //Assert
        coEvery { getDocByIdUseCase(any()) } returns FakeRepository.fakeDoc
        coEvery { updateLocalDoc(any()) } returns Unit
        coEvery { updateRemoteDocNameUseCase(any(),any()) } returns Unit
        val data =
            workDataOf(
                AppConstants.LOCAL_ID_KEY to 10L,
                AppConstants.REMOTE_ID_KEY to 10L,
                AppConstants.DOC_NAME_ID_KEY to "dummyName",
                )
        val worker = getWorker(data)
        runBlocking {
            //Act
            val result = worker.doWork()
            val outputData = worker.inputData

            //Assert
            MatcherAssert.assertThat(result, Matchers.`is`(ListenableWorker.Result.success()))
        }
    }

    private inner class UpdateDocNameWorkerFactory: WorkerFactory() {
        override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters): ListenableWorker {
            return UpdateDocNameWorker(
                appContext,
                workerParameters,
                dispatcher,
                updateLocalDoc,
                updateRemoteDocNameUseCase,
                getDocByIdUseCase
            )
        }
    }
}