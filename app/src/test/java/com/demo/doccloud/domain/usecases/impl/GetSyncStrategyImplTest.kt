package com.demo.doccloud.domain.usecases.impl

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.demo.doccloud.data.datasource.local.AppLocalServices
import com.demo.doccloud.data.datasource.remote.FirebaseServices
import com.demo.doccloud.data.repository.RepositoryImpl
import com.demo.doccloud.domain.entities.SyncStrategy
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [30])
@RunWith(RobolectricTestRunner::class)
class GetSyncStrategyImplTest {
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `return syncStrategy model`() = runBlocking {
        //Arrange
        val remoteDatasourceMock = mockk<FirebaseServices>()
        val localDatasourceMock = mockk<AppLocalServices>()
        val repository = RepositoryImpl(
            remoteDatasource = remoteDatasourceMock,
            localDatasource = localDatasourceMock,
            context
        )
        val getSyncStrategy = GetSyncStrategyImpl(repository)
        coEvery{remoteDatasourceMock.getSyncStrategy()} returns SyncStrategy(
            expiration = -1L,
            lastUpdated = -1L,
            customId = -1L
        )

        //Act
        val result = getSyncStrategy()

        //Assert
        assertThat(result).isInstanceOf(SyncStrategy::class.java)
    }

}