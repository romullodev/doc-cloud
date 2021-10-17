package com.demo.doccloud.domain.usecases.impl

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.demo.doccloud.data.datasource.local.AppLocalServices
import com.demo.doccloud.data.datasource.local.persist.SharedPreferenceImpl
import com.demo.doccloud.data.datasource.local.room.AppDatabase
import com.demo.doccloud.data.datasource.local.room.entities.DatabaseDoc
import com.demo.doccloud.data.datasource.remote.RemoteDataSource
import com.demo.doccloud.data.repository.RepositoryImpl
import com.demo.doccloud.domain.entities.DocStatus
import com.demo.doccloud.domain.entities.Photo
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UpdateLocalDocPhotoImplTest {
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `check if photo was update with NOT_SENT status`() = runBlocking {
        //Arrange
        val appDatabaseMock = mockk<AppDatabase>()
        val sharedPreferenceMock = mockk<SharedPreferenceImpl>()
        val localDatasourceMock = AppLocalServices(
            dispatcher = Dispatchers.Default,
            appDatabase = appDatabaseMock,
            persistSimpleData = sharedPreferenceMock
        )
        val remoteDatabaseDoc = mockk<RemoteDataSource>()
        val repository = RepositoryImpl(
            remoteDatabaseDoc,
            localDatasourceMock,
            context
        )
        val updateLocalDocPhoto = UpdateLocalDocPhotoImpl(repository)
        val oldPhoto = Photo(id = 5L, path = "old")
        val oldDatabaseDoc = DatabaseDoc(
            remoteId = -10L,
            name = "",
            date = "",
            pages = listOf(oldPhoto),
            status = DocStatus.SENT,
            localId = -20L
        )
        coEvery { appDatabaseMock.docDao.getDoc(any()) } returns oldDatabaseDoc
        val docSlot = slot<DatabaseDoc>()
        coEvery { appDatabaseMock.docDao.update(capture(docSlot)) } returns mockk()
        val dummyId = -1L
        val newPhoto = Photo(id = 5L, path = "new")
        val newDatabaseDoc = oldDatabaseDoc.copy(pages = listOf(newPhoto), status = DocStatus.NOT_SENT)

        //Act
        updateLocalDocPhoto(dummyId, newPhoto)

        //Assert
        verify { appDatabaseMock.docDao.update(eq(newDatabaseDoc)) }

    }
}