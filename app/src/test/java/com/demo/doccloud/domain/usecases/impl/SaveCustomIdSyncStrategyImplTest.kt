package com.demo.doccloud.domain.usecases.impl

import com.demo.doccloud.data.repository.RepositoryImpl
import com.demo.doccloud.utils.AppConstants
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import org.junit.Test

class SaveCustomIdSyncStrategyImplTest {

    @Test
    fun `check parameters previously defined`() = runBlocking{
        //Arrange
        val repository = mockk<RepositoryImpl>()
        val saveCustomIdSyncStrategy = SaveCustomIdSyncStrategyImpl(repository)
        val keySlot = slot<String>()
        val defaultSlot = slot<Long>()
        coEvery { repository.saveLong(capture(keySlot), any())} returns mockk()

        //Act
        saveCustomIdSyncStrategy()

        //Assert
        coVerify { repository.saveLong(eq(AppConstants.LOCAL_DATABASE_CUSTOM_ID_KEY), any()) }
    }

}