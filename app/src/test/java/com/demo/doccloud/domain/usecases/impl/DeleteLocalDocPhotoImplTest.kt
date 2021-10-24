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
import org.robolectric.annotation.Config

@Config(sdk = [30])
@RunWith(RobolectricTestRunner::class)
class DeleteLocalDocPhotoImplTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `check via parameter if the photo was deleted`() = runBlocking {
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
        val deleteLocalDocPhotoImpl = DeleteLocalDocPhotoImpl(repository)
        val photo = Photo(id = 1L, path = "")
        val deletedPhoto = photo.copy(id = 2L)
        val photos = arrayListOf(photo, deletedPhoto)
        val databaseDoc = DatabaseDoc(
            remoteId = 1L,
            name = "",
            date = "",
            pages = photos.toList(),
            status = DocStatus.NOT_SENT
        )
        val updatedDatabaseDoc = databaseDoc.copy(pages = listOf(photo))
        val dummyId = -1L
        coEvery { appDatabaseMock.docDao.getDoc(any())} returns databaseDoc
        val docSlot = slot<DatabaseDoc>()
        coEvery { appDatabaseMock.docDao.update(capture(docSlot))} returns mockk()

        //Act
        deleteLocalDocPhotoImpl(dummyId, deletedPhoto)

        //Assert
        coVerify {
            appDatabaseMock.docDao.update(updatedDatabaseDoc)
        }
    }

}