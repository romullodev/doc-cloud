package com.demo.doccloud.domain.usecases.impl

import com.demo.doccloud.data.repository.Repository
import com.demo.doccloud.data.repository.RepositoryImpl
import com.demo.doccloud.utils.AppConstants
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class SaveCustomIdSyncStrategyImplTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var repository: RepositoryImpl

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun check_key_parameter_previously_defined() = runBlocking{
        //Arrange
        val repository = mockk<Repository>()
        val saveCustomIdSyncStrategy = SaveCustomIdSyncStrategyImpl(repository)
        val keySlot = slot<String>()
        val defaultSlot = slot<Long>()
        coEvery { repository.saveLong(capture(keySlot), any())} returns mockk()

        //Act
        saveCustomIdSyncStrategy()

        //Assert
        coVerify { repository.saveLong(eq(AppConstants.LOCAL_DATABASE_CUSTOM_ID_KEY), any()) }
    }

    @Test
    fun get_value_previously_saved() = runBlocking{
        //Arrange
        val saveCustomIdSyncStrategy = SaveCustomIdSyncStrategyImpl(repository)
        val getSavedCustomIdSyncStrategy = GetSavedCustomIdSyncStrategyImpl(repository)


        //Act
        saveCustomIdSyncStrategy()
        val id = getSavedCustomIdSyncStrategy()

        //Assert
        assert(id != AppConstants.DATABASE_DEFAULT_CUSTOM_ID)
    }

}