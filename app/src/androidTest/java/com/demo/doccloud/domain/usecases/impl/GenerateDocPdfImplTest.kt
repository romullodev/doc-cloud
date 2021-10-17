package com.demo.doccloud.domain.usecases.impl

import FileUtil
import androidx.test.platform.app.InstrumentationRegistry
import com.demo.doccloud.MainCoroutineRule
import com.demo.doccloud.domain.entities.Doc
import com.demo.doccloud.domain.entities.DocStatus
import com.demo.doccloud.domain.entities.Photo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test


@ExperimentalCoroutinesApi
class GenerateDocPdfImplTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Test
    fun runGeneratePdf() = mainCoroutineRule.runBlockingTest {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val generateDocPdfImpl = GenerateDocPdfImpl(appContext, Dispatchers.Main)

//        val inputStream: InputStream = appContext.resources.openRawResource(R.raw.img_test)//"android.resource://${appContext.packageName}/raw/img_test.jpg"
//        val bm: Bitmap = BitmapFactory.decodeStream(inputStream)
//        val file = File("${appContext.cacheDir}/img_test.jpg")
//        val os: OutputStream = BufferedOutputStream(FileOutputStream(file))
//        bm.compress(Bitmap.CompressFormat.JPEG, 100, os)
//        os.close()

        val doc = Doc(
            remoteId = -1L,
            name = "any name",
            date = "",
            pages = listOf(
                Photo(1L, FileUtil.getStubFile(appContext).path)
            ),
            status = DocStatus.NOT_SENT,
        )
        val pdfFile = generateDocPdfImpl(doc)
        assert(pdfFile.isFile)
    }
}