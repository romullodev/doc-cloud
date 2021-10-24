package com.demo.doccloud.domain.usecases.impl

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.demo.doccloud.data.datasource.local.AppLocalServices
import com.demo.doccloud.data.datasource.local.persist.SharedPreferenceImpl
import com.demo.doccloud.data.datasource.local.room.AppDatabase
import com.demo.doccloud.data.datasource.local.room.entities.DatabaseDoc
import com.demo.doccloud.data.datasource.remote.RemoteDataSource
import com.demo.doccloud.data.repository.RepositoryImpl
import com.demo.doccloud.domain.entities.Doc
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
class UpdateLocalDocImplTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `check if updated method with the correct param was called`() = runBlocking {
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
        val doc = Doc(
            remoteId = -10L,
            name = "",
            date = "",
            pages = listOf(),
            status = DocStatus.NOT_SENT,
            localId = -20L
        )

        val databaseDoc = DatabaseDoc(
            remoteId = -10L,
            name = "",
            date = "",
            pages = listOf(),
            status = DocStatus.NOT_SENT,
            localId = -20L
        )
        val docSlot = slot<DatabaseDoc>()
        coEvery { appDatabaseMock.docDao.update(capture(docSlot)) } returns mockk()
        val updateLocalDoc = UpdateLocalDocImpl(repository)

        //Act
        updateLocalDoc(doc)

        //Assert
        verify { appDatabaseMock.docDao.update(eq(databaseDoc)) }
    }
}


