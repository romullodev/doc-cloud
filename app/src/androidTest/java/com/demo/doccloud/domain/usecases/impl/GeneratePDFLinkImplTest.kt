package com.demo.doccloud.domain.usecases.impl

import FileUtil
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.demo.doccloud.data.repository.Repository
import com.demo.doccloud.domain.entities.Doc
import com.demo.doccloud.domain.entities.DocStatus
import com.demo.doccloud.domain.entities.Photo
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
@ExperimentalCoroutinesApi
class GeneratePDFLinkImplTest{

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private var repository: Repository = mockk()
    private lateinit var appContext: Context

    @Before
    fun setup(){
        hiltRule.inject()

        appContext = InstrumentationRegistry.getInstrumentation().targetContext

        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()
        // Initialize WorkManager for instrumentation tests.
        WorkManagerTestInitHelper.initializeTestWorkManager(appContext, config)
    }

    @Test
    fun getPdfLink() : Unit = runBlocking{
        //Arrange
        val generateDocPdfImpl = GenerateDocPdfImpl(appContext, Dispatchers.Default)
        val scheduleToRemoveTempFile = ScheduleToRemoveTempFileImpl(appContext)
        val getRemoveTempFileTime = GetRemoveTempFileTimeImpl(repository)
        val generatePDFLinkImpl = GeneratePDFLinkImpl(generateDocPdfImpl, scheduleToRemoveTempFile, getRemoveTempFileTime, repository)
        val doc = Doc(
            remoteId = -1L,
            name = "any name",
            date = "",
            pages = listOf(
                Photo(1L, FileUtil.getStubFile(appContext).path)
            ),
            status = DocStatus.NOT_SENT,
        )
        coEvery { repository.generatePDFLink(any(), any()) } returns Uri.EMPTY
        coEvery { repository.getRemoveTempFileTime() } returns 1L

        //Act
        val uri: Uri = generatePDFLinkImpl(doc)

        //Assert
        assertTrue(uri == Uri.EMPTY)
    }

}