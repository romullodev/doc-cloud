package com.demo.doccloud.domain.usecases.impl

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.demo.doccloud.data.repository.RepositoryImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SyncDataImplTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `call just one time clearAllData,  and insertDocs methods`() = runBlocking{
        //Arrange
        val repository = spyk(
            mockk<RepositoryImpl>()
        )
        val dummyId = -1L
        val syncDataImpl = SyncDataImpl(repository)
        coEvery { repository.syncData(dummyId) } returns listOf()
        coEvery { repository.clearDocs() } returns mockk()
        coEvery { repository.insertDocs(listOf()) } returns mockk()

        //Act
        syncDataImpl(dummyId)

        //Assert
        coVerify { repository.clearDocs() }
        coVerify { repository.insertDocs(any()) }
    }
}