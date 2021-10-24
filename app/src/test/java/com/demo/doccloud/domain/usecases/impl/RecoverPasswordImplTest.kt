package com.demo.doccloud.domain.usecases.impl

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.demo.doccloud.data.datasource.local.AppLocalServices
import com.demo.doccloud.data.datasource.local.LocalDataSource
import com.demo.doccloud.data.datasource.remote.FirebaseServices
import com.demo.doccloud.data.repository.RepositoryImpl
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [30])
@RunWith(RobolectricTestRunner::class)
class RecoverPasswordImplTest {
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `check if passed input is correct `() = runBlocking {
        //Arrange
        val remoteDatasourceMock = mockk<FirebaseServices>()
        val localDatasourceMock = mockk<AppLocalServices>()
        val repository = RepositoryImpl(
            remoteDatasource = remoteDatasourceMock,
            localDatasource = localDatasourceMock,
            context
        )
        val recoverPasswordUseCase = RecoverPasswordImpl(repository)
        val email = "romulo_dasilva@hotmail.com"
        val slot = slot<String>()
        coEvery { remoteDatasourceMock.recoverPassword(capture(slot)) } returns mockk()

        //Act
        recoverPasswordUseCase(email)

        //Assert
        coVerify {
            remoteDatasourceMock.recoverPassword(eq(email))
        }
    }
}
