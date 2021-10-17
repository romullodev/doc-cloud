package com.demo.doccloud.domain.usecases.impl

import FileUtil.getStubFile
import android.content.Context
import androidx.core.net.toUri
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowContentResolver
import java.io.ByteArrayInputStream

@RunWith(RobolectricTestRunner::class)
class CopyFileImplTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `run and return a valid File`() = runBlocking {
        //Arrange
        val copyFile = CopyFileImpl(context, Dispatchers.Default)
//        val tempFolder = TemporaryFolder()
//        tempFolder.create()
//        val tempFile = tempFolder.newFile()
        val stubFile = getStubFile(context)
        val uri = stubFile.toUri()
        val shadowContentResolver: ShadowContentResolver
        val contentResolver = ApplicationProvider.getApplicationContext<Context>().contentResolver
        shadowContentResolver = Shadows.shadowOf(contentResolver)
        shadowContentResolver.registerInputStream(
            uri,
            ByteArrayInputStream("ourStream".toByteArray())
        )
        // Act
        val file = copyFile(stubFile.toUri())

        // Assert
        Truth.assertThat(file?.isFile).isTrue()
    }

}