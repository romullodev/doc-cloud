package com.demo.doccloud.domain.usecases.impl

import com.demo.doccloud.data.repository.RepositoryImpl
import com.demo.doccloud.domain.usecases.contracts.GetRemoveTempFileTime
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class GetRemoveTempFileTimeImplTest{

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var repositoryImpl: RepositoryImpl

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun getTempFileTime(): Unit = runBlocking{
        //Arrange
        val getRemoveTempFileTime = GetRemoveTempFileTimeImpl(repositoryImpl)

        //Act
        val time = getRemoveTempFileTime()

        //Assert
        assertTrue(time == 30L)
    }


}