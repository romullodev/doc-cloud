package com.demo.doccloud.domain.usecases

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.test.core.app.ApplicationProvider
import com.demo.doccloud.*
import com.demo.doccloud.FakeRepository
import com.demo.doccloud.domain.entities.Doc
import com.demo.doccloud.domain.entities.DocStatus
import com.demo.doccloud.domain.entities.Photo
import com.demo.doccloud.domain.usecases.impl.*
import com.google.common.truth.Truth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowContentResolver
import java.io.ByteArrayInputStream

@RunWith(RobolectricTestRunner::class)
@ExperimentalCoroutinesApi
class UseCasesTest {

    private lateinit var context: Context
    private lateinit var repository: FakeRepository
    private lateinit var doc: Doc
    private lateinit var photo: Photo

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        repository = FakeRepository(context)
        doc = Doc(
            remoteId = -1L,
            name = "any name",
            date = "",
            pages = listOf(),
            status = DocStatus.NOT_SENT,
        )
        photo = Photo(id = -1L, path = "any")
    }
    /**
     * test addPhotosToLocalDocUseCase too
     */
    @Test
    fun `run addPhotosToLocalDoc and addPhotosToLocalDoc`() = mainCoroutineRule.runBlockingTest {
        val addPhotosToLocalDoc = AddPhotosToLocalDocImpl(repository)
        val fakeScheduleToAddRemoteDocPhotos = FakeScheduleToAddRemoteDocPhotosImpl()
        val addPhotos = AddPhotosImpl(addPhotosToLocalDoc, fakeScheduleToAddRemoteDocPhotos)
        addPhotos(localId = -1L, photos = listOf())
    }

    @Test
    fun `run addPhotoToRemoteDoc`() = mainCoroutineRule.runBlockingTest {
        val addPhotoToRemoteDoc = AddPhotoToRemoteDocImpl(repository)
        addPhotoToRemoteDoc(remoteId = -1L, photos = listOf(), newJsonPages = "any")
    }

    @Test
    fun `run copyFile and return a valide File`() = mainCoroutineRule.runBlockingTest {
        val copyFile = CopyFileImpl(context, Dispatchers.Main)
        val tempFolder = TemporaryFolder()
        tempFolder.create()
        val tempFile = tempFolder.newFile()
        val uri = tempFile.toUri()
        val shadowContentResolver: ShadowContentResolver
        val contentResolver = ApplicationProvider.getApplicationContext<Context>().contentResolver
        shadowContentResolver = shadowOf(contentResolver)
        shadowContentResolver.registerInputStream(
            uri,
            ByteArrayInputStream("ourStream".toByteArray())
        );
        val file = copyFile(uri)
        Truth.assertThat(file?.isFile).isTrue()
    }

    /**
     * test DeleteLocalDocUseCase too
     */
    @Test
    fun `run deleteDoc and DeleteLocalDocUseCase`() = mainCoroutineRule.runBlockingTest {
        val deleteLocalDoc = DeleteLocalDocImpl(repository)
        val fakeScheduleToDeleteRemoteDoc = FakeScheduleToDeleteRemoteDocImpl()
        val deleteDoc = DeleteDocImpl(deleteLocalDoc, fakeScheduleToDeleteRemoteDoc, context)

        val result = deleteDoc(doc)
        Truth.assertThat(result.contains(
            ApplicationProvider.getApplicationContext<Context>().getString(R.string.home_toast_delete_success, doc.name)
        ))
    }

    /**
     * test deleteLocalDocPhoto too
     */
    @Test
    fun `run deleteDocPhoto and deleteLocalDocPhoto`() = mainCoroutineRule.runBlockingTest {
        val deleteLocalDocPhoto = DeleteLocalDocPhotoImpl(repository)
        val fakeScheduleToDeleteRemoteDocPhoto = FakeScheduleToDeleteRemoteDocPhotoImpl()
        val deleteDocPhoto = DeleteDocPhotoImpl(deleteLocalDocPhoto, fakeScheduleToDeleteRemoteDocPhoto)
        deleteDocPhoto(-1L, Photo(-1L, "any"))
    }

    @Test
    fun `run deleteRemoteDoc`() = mainCoroutineRule.runBlockingTest {
        val deleteRemoteDoc = DeleteRemoteDocImpl(repository)
        deleteRemoteDoc(-1L, listOf())
    }

    @Test
    fun `run deleteRemoteDocPhoto`() = mainCoroutineRule.runBlockingTest {
        val deleteRemoteDocPhoto = DeleteRemoteDocPhotoImpl(repository)
        deleteRemoteDocPhoto(-1L, Photo(-1L, "any"), "any")
    }

    /**
     * test saveCustomIdSyncStrategy too
     */
    @Test
    fun `run doLoginWithGoogle and saveCustomIdSyncStrategy`() = mainCoroutineRule.runBlockingTest {
        val saveCustomIdSyncStrategy = SaveCustomIdSyncStrategyImpl(repository)
        val doLoginWithGoogle = DoLoginWithGoogleImpl(saveCustomIdSyncStrategy, repository)
        doLoginWithGoogle(Intent("any"))
    }

    @Test
    fun `run doLogout`() = mainCoroutineRule.runBlockingTest {
        val doLogoutImpl = DoLogoutImpl(repository)
        doLogoutImpl()
    }
    /**
     * test generateDocPdfImpl is test under androidTest folder
     */

    @Test
    fun `run GetAllDocs`() = mainCoroutineRule.runBlockingTest {
        val getAllDocs = GetAllDocsImpl(repository)
        getAllDocs()
    }

    @Test
    fun `run GetDocById`() = mainCoroutineRule.runBlockingTest {
        val getDocById = GetDocByIdImpl(repository)
        getDocById(-1L)
    }

    @Test
    fun `run getSavedCustomIdSyncStrategy`() = mainCoroutineRule.runBlockingTest {
        val getSavedCustomIdSyncStrategy = GetSavedCustomIdSyncStrategyImpl(repository)
        getSavedCustomIdSyncStrategy()
    }

    @Test
    fun `run getSyncStrategy`() = mainCoroutineRule.runBlockingTest {
        val getSyncStrategy = GetSyncStrategyImpl(repository)
        getSyncStrategy()
    }

    @Test
    fun `run getUser`() = mainCoroutineRule.runBlockingTest {
        val getUser = GetUserImpl(repository)
        getUser()
    }

    /**
     * test SaveLocalDoc too
     */

    @Test
    fun `run saveDoc and SaveLocalDoc`() = mainCoroutineRule.runBlockingTest {
        val saveLocalDoc = SaveLocalDocImpl(repository)
        val fakeScheduleToSaveRemoteDocImpl = FakeScheduleToSaveRemoteDocImpl()
        val saveDoc = SaveDocImpl(saveLocalDoc, fakeScheduleToSaveRemoteDocImpl)
        saveDoc(doc)
    }

    @Test
    fun `run syncDataImpl`() = mainCoroutineRule.runBlockingTest {
        val syncDataImpl = SyncDataImpl(repository)
        syncDataImpl(customId = -1L)
    }

    /**
     * test UpdateLocalDocName too
     */
    @Test
    fun `run updatedDocName`() = mainCoroutineRule.runBlockingTest {
        val fakeScheduleToUpdateRemoteDocNameImpl = FakeScheduleToUpdateRemoteDocNameImpl()
        val updateLocalDocName = UpdateLocalDocNameImpl(repository)
        val updatedDocName = UpdatedDocNameImpl(fakeScheduleToUpdateRemoteDocNameImpl, updateLocalDocName)
        updatedDocName(localId = -1L, remoteId = -1L,  name = "any")
    }
    /**
     * test updateLocalDocPhoto too
     */
    @Test
    fun `run updateDocPhoto and updateLocalDocPhoto`() = mainCoroutineRule.runBlockingTest {
        val updateLocalDocPhoto = UpdateLocalDocPhotoImpl(repository)
        val fakeScheduleToUpdateRemoteDocPhoto = FakeScheduleToUpdateRemoteDocPhotoImpl()
        val updateDocPhotoImpl = UpdateDocPhotoImpl(updateLocalDocPhoto, fakeScheduleToUpdateRemoteDocPhoto)
        updateDocPhotoImpl(localId = -1L, photo)
    }

    @Test
    fun `run UpdateLocalDoc`() = mainCoroutineRule.runBlockingTest {
        val updateLocalDocImpl = UpdateLocalDocImpl(repository)
        updateLocalDocImpl(doc)
    }

    @Test
    fun `run updateRemoteDocNameImpl`() = mainCoroutineRule.runBlockingTest {
        val updateRemoteDocNameImpl = UpdateRemoteDocNameImpl(repository)
        updateRemoteDocNameImpl(remoteId = -1L, name = "any")
    }

    @Test
    fun `run updateRemoteDocPhoto`() = mainCoroutineRule.runBlockingTest {
        val updateRemoteDocPhoto = UpdateRemoteDocPhotoImpl(repository)
        updateRemoteDocPhoto(remoteId = -1L, photo = photo)
    }

    @Test
    fun `run uploadDoc`() = mainCoroutineRule.runBlockingTest {
        val uploadDoc = UploadDocImpl(repository)
        uploadDoc(doc)
    }

}