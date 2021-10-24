package com.demo.doccloud.domain.usecases.impl

import com.demo.doccloud.data.repository.RepositoryImpl
import com.demo.doccloud.utils.AppConstants
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.robolectric.annotation.Config

class GetSavedCustomIdSyncStrategyImplTest {

    @Test
    fun `check parameters previously defined`() = runBlocking{
        //Arrange
        val repository = mockk<RepositoryImpl>()
        val getSavedCustomIdSyncStrategy = GetSavedCustomIdSyncStrategyImpl(repository)
        val keySlot = slot<String>()
        val defaultSlot = slot<Long>()
        coEvery { repository.getLong(capture(keySlot), capture(defaultSlot))} returns -1L

        //Act
        getSavedCustomIdSyncStrategy()

        //Assert
        coVerify { repository.getLong(eq(AppConstants.LOCAL_DATABASE_CUSTOM_ID_KEY), eq(AppConstants.DATABASE_DEFAULT_CUSTOM_ID)) }
    }

}