package com.demo.doccloud.domain.usecases.impl

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.demo.doccloud.data.datasource.local.AppLocalServices
import com.demo.doccloud.data.datasource.local.persist.SharedPreferenceImpl
import com.demo.doccloud.data.datasource.local.room.AppDatabase
import com.demo.doccloud.data.datasource.local.room.entities.DatabaseDoc
import com.demo.doccloud.data.datasource.remote.FirebaseServices
import com.demo.doccloud.data.repository.RepositoryImpl
import com.demo.doccloud.domain.entities.DocStatus
import com.demo.doccloud.domain.entities.Photo
import com.demo.doccloud.domain.usecases.contracts.AddPhotosToLocalDoc
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AddPhotosToLocalDocImplTest {
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }


    @Test
    fun `check if params has Doc with added photos`() = runBlocking{
        //Arrange
        val remoteDatasourceMock = mockk<FirebaseServices>()
        val appDatabaseMock = mockk<AppDatabase>()
        val sharedPreferenceMock = mockk<SharedPreferenceImpl>()
        val localDatasourceMock = AppLocalServices(
            dispatcher = Dispatchers.Default,
            appDatabase = appDatabaseMock,
            persistSimpleData = sharedPreferenceMock
        )
        val repository = RepositoryImpl(
            remoteDatasource = remoteDatasourceMock,
            localDatasource = localDatasourceMock,
            context
        )
        val addPhotosToLocalDoc = AddPhotosToLocalDocImpl(repository)

        val slot = slot<DatabaseDoc>()
        coEvery { appDatabaseMock.docDao.update(capture(slot)) } returns mockk()
        val photosFromDb = arrayListOf(Photo(id = 1L, path = ""), Photo(id = 2L, path = ""))
        val databaseDocFromDb = DatabaseDoc(
            remoteId = 1L,
            name = "",
            date = "",
            pages = photosFromDb.toList(),
            status = DocStatus.NOT_SENT
        )
        coEvery { appDatabaseMock.docDao.getDoc(any()) } returns databaseDocFromDb
        val localIdDummy = 1L
        val newPhotos = listOf(Photo(id = 3L, path = ""), Photo(id = 4L, path = ""))
        photosFromDb.addAll(newPhotos)
        val databaseDocUpdated = databaseDocFromDb.copy(pages = photosFromDb)

        //Act
        addPhotosToLocalDoc(localIdDummy, newPhotos)

        //Assert
        coVerify {
            appDatabaseMock.docDao.update(eq(databaseDocUpdated))
        }
    }
}