package com.demo.doccloud.domain.usecases.impl

import com.demo.doccloud.data.repository.RepositoryImpl
import com.demo.doccloud.domain.entities.AppLicense
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class GetAppLicencesFromServerImplTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var repositoryImpl: RepositoryImpl

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun get_all_app_licences() : Unit = runBlocking{
        //Arrange
        val getAppLicencesFromServer = GetAppLicencesFromServerImpl(repositoryImpl)

        //Act
        val licences: List<AppLicense> = getAppLicencesFromServer()

        //Assert
        Assert.assertTrue(licences.size == 6)
    }
}