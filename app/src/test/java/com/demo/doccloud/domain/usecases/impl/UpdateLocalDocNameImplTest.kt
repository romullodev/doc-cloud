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
import org.robolectric.annotation.Config

@Config(sdk = [30])
@RunWith(RobolectricTestRunner::class)
class UpdateLocalDocNameImplTest{

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }


    @Test
    fun `check if doc was update with the NOT_SENT status`() = runBlocking {
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
        val updateLocalDocName = UpdateLocalDocNameImpl(repository)
        val databaseDoc = DatabaseDoc(
            remoteId = -10L,
            name = "",
            date = "",
            pages = listOf(),
            status = DocStatus.SENT,
            localId = -20L
        )
        coEvery { appDatabaseMock.docDao.getDoc(any()) } returns databaseDoc
        val docSlot = slot<DatabaseDoc>()
        coEvery { appDatabaseMock.docDao.update(capture(docSlot)) } returns mockk()
        val dummyId = -1L
        val newName = "new Name"
        val databaseDocUpdated = databaseDoc.copy(name = newName,status = DocStatus.NOT_SENT)

        //Act
        updateLocalDocName(localId = dummyId, name = newName)

        //Assert
        verify { appDatabaseMock.docDao.update(databaseDocUpdated) }
    }
}