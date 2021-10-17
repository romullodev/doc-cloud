package com.demo.doccloud.domain.usecases.impl

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.demo.doccloud.data.datasource.local.AppLocalServices
import com.demo.doccloud.data.datasource.remote.FirebaseServices
import com.demo.doccloud.data.repository.RepositoryImpl
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DoLogoutImplTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `call just one time clearAllData and do doLogout methods`() = runBlocking{
        //Arrange
        val remoteDatasourceMock = spyk(
            mockk<FirebaseServices>()
        )
        val localDatasourceMock = spyk(mockk<AppLocalServices>())
        val repository = RepositoryImpl(
            remoteDatasource = remoteDatasourceMock,
            localDatasource = localDatasourceMock,
            context
        )
        val doLogoutImpl = DoLogoutImpl(repository)
        coEvery { localDatasourceMock.clearAllData() } returns mockk()
        coEvery { remoteDatasourceMock.doLogout() } returns mockk()

        //Act
        doLogoutImpl()

        //Assert
        coVerify { localDatasourceMock.clearAllData() }
        coVerify { remoteDatasourceMock.doLogout() }
    }
}