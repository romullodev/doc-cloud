package com.demo.doccloud.ui.edit

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.demo.doccloud.*
import com.demo.doccloud.FakeRepository
import com.demo.doccloud.domain.entities.Doc
import com.demo.doccloud.domain.usecases.impl.*
import com.demo.doccloud.ui.crop.CropViewModel
import com.demo.doccloud.utils.BackToRoot
import com.demo.doccloud.utils.ListPhotoArg
import com.demo.doccloud.utils.RootDestination
import com.google.common.truth.Truth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@ExperimentalCoroutinesApi
class EditViewModelTest {

    private lateinit var repository: FakeRepository
    private lateinit var editViewModel: EditViewModel
    private lateinit var doc: Doc
    private lateinit var context: Context

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        repository = FakeRepository(context)

        val copyFileUseCase = FakeCopyFileImpl()
        val generateDocPdfUseCase = FakeGenerateDocPdfImpl(Dispatchers.Main, context)
        val getDocByIdUseCase = GetDocByIdImpl(repository)

        val fakeScheduleToUpdateRemoteDocName = FakeScheduleToUpdateRemoteDocNameImpl()
        val updateLocalDocName = UpdateLocalDocNameImpl(repository)
        val updatedDocNameUseCase = UpdatedDocNameImpl(fakeScheduleToUpdateRemoteDocName, updateLocalDocName)

        val deleteLocalDocPhoto = DeleteLocalDocPhotoImpl(repository)
        val fakeScheduleToDeleteRemoteDocPhoto = FakeScheduleToDeleteRemoteDocPhotoImpl()
        val deleteDocPhotoUseCase = DeleteDocPhotoImpl(deleteLocalDocPhoto, fakeScheduleToDeleteRemoteDocPhoto)

        val updateLocalDocPhoto = UpdateLocalDocPhotoImpl(repository)
        val fakeScheduleToUpdateRemoteDocPhoto = FakeScheduleToUpdateRemoteDocPhotoImpl()
        val updateDocPhoto = UpdateDocPhotoImpl(updateLocalDocPhoto, fakeScheduleToUpdateRemoteDocPhoto)

        doc = FakeRepository.fakeDoc.copy(localId = 1)
        editViewModel = EditViewModel(
            copyFileUseCase,
            generateDocPdfUseCase,
            getDocByIdUseCase,
            updatedDocNameUseCase,
            deleteDocPhotoUseCase,
            updateDocPhoto
        )
        //this method is execute on onResume state of the fragment
        editViewModel.getDocById(id = 1L)
    }

    @After
    fun teardown(){
        GlobalVariablesTest.clearFlags()
        repository.clearFlags()
    }

    @Test
    fun `update doc name with success`(){
        editViewModel.updateNameDoc(localId = -1L, remoteId = -1L, newName = "any")
    }

    @Test
    fun `throw exception when update doc name`(){
        repository.setShouldThrowUnknownException(true)
        editViewModel.updateNameDoc(localId = 1L, remoteId = -1L, newName = "any")
        val value = editViewModel.editState.getOrAwaitValue()
        Truth.assertThat((value.getContentIfNotHandled() as EditViewModel.EditState.EditAlertDialog).msg).isEqualTo(
            R.string.common_unknown_error
        )
    }

    @Test
    fun `get doc by id with success`(){
        val value = editViewModel.doc.getOrAwaitValue()
        Truth.assertThat(value).isNotNull()
    }

    @Test
    fun `share doc with success`(){
        editViewModel.shareDoc()
        val value = editViewModel.editState.getOrAwaitValue()
        Truth.assertThat((value.getContentIfNotHandled() as EditViewModel.EditState.SharePdf).data.isFile)
    }

    @Test
    fun `throw exception when share doc`(){
        GlobalVariablesTest.shouldThrowException = true
        editViewModel.shareDoc()
        val value = editViewModel.editState.getOrAwaitValue()
        Truth.assertThat((value.getContentIfNotHandled() as EditViewModel.EditState.EditAlertDialog).msg).isEqualTo(
            R.string.home_alert_error_generate_pdf
        )
    }


    @Test
    fun `copy and navigate to CropFragment with success`(){
        editViewModel.copyAndNavigateToCrop(listOf())
        val value = editViewModel.navigationCommands.getOrAwaitValue()
        Truth.assertThat((value.getContentIfNotHandled() as EditViewModel.NavigationCommand.To).directions).isEqualTo(
            EditFragmentDirections.actionGlobalCropFragment(
                photos = ListPhotoArg(ArrayList()),
                root = BackToRoot(
                    rootDestination = RootDestination.EDIT_DESTINATION,
                    localId = doc.localId
                )
            )
        )
    }

    @Test
    fun `throw exception when copy and navigate to CropFragment`(){
        GlobalVariablesTest.shouldThrowException = true
        editViewModel.copyAndNavigateToCrop(listOf(Uri.EMPTY))
        val value = editViewModel.editState.getOrAwaitValue()
        Truth.assertThat((value.getContentIfNotHandled() as EditViewModel.EditState.EditAlertDialog).msg).isEqualTo(
            R.string.home_alert_error_copy_image_from_gallery
        )
    }

    //R.string.home_alert_error_copy_image_from_gallery

    @Test
    fun `delete doc photo with success`(){
        editViewModel.deleteSelectedDocPhoto()
    }

    @Test
    fun `throw unknown exception when delete doc photo`(){
        repository.setShouldThrowUnknownException(true)
        editViewModel.deleteSelectedDocPhoto()
        val value = editViewModel.editState.getOrAwaitValue()
        Truth.assertThat((value.getContentIfNotHandled() as EditViewModel.EditState.EditAlertDialog).msg).isEqualTo(
            R.string.common_unknown_error
        )
    }

    @Test
    fun `update doc photo with success`(){
        editViewModel.updateDocPhoto(Uri.EMPTY)
    }

    @Test
    fun `throw unknown exception when update doc photo`(){
        GlobalVariablesTest.shouldThrowException = true
        editViewModel.updateDocPhoto(Uri.EMPTY)
        val value = editViewModel.cropState.getOrAwaitValue()
        Truth.assertThat((value.getContentIfNotHandled() as EditViewModel.EditCropState.CropAlertDialog).msg).isEqualTo(
            R.string.common_unknown_error
        )
    }
}