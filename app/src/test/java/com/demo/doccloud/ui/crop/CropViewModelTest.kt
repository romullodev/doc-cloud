package com.demo.doccloud.ui.crop

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.demo.doccloud.*
import com.demo.doccloud.FakeRepository
import com.demo.doccloud.domain.entities.Photo
import com.demo.doccloud.domain.usecases.impl.AddPhotosImpl
import com.demo.doccloud.domain.usecases.impl.AddPhotosToLocalDocImpl
import com.demo.doccloud.domain.usecases.impl.SaveDocImpl
import com.demo.doccloud.domain.usecases.impl.SaveLocalDocImpl
import com.google.common.truth.Truth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [30])
@RunWith(RobolectricTestRunner::class)
@ExperimentalCoroutinesApi
class CropViewModelTest {

    private lateinit var repository: FakeRepository
    private lateinit var cropViewModel: CropViewModel
    private lateinit var context: Context

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        repository = FakeRepository(context)

        val saveLocalDoc = SaveLocalDocImpl(repository)
        val fakeScheduleToSaveRemoteDocImpl = FakeScheduleToSaveRemoteDocImpl()
        val saveDocUseCase = SaveDocImpl(saveLocalDoc, fakeScheduleToSaveRemoteDocImpl)

        val addPhotosToLocalDoc = AddPhotosToLocalDocImpl(repository)
        val fakeScheduleToAddRemoteDocPhotos = FakeScheduleToAddRemoteDocPhotosImpl()
        val addPhotosUseCase = AddPhotosImpl(addPhotosToLocalDoc, fakeScheduleToAddRemoteDocPhotos)

        val copyFileUseCase = FakeCopyFileImpl()

        cropViewModel = CropViewModel(
            saveDocUseCase,
            addPhotosUseCase,
            copyFileUseCase
        )
        cropViewModel.setListPhoto(arrayListOf(
            Photo(
                id = -1L,
                path = Uri.EMPTY.path!!
            ),
            Photo(
                id = -2L,
                path = Uri.EMPTY.path!!
            )
        ))
    }

    @After
    fun teardown(){
        GlobalVariablesTest.clearFlags()
        repository.clearFlags()
    }

    @Test
    fun `save doc with success`() {
        cropViewModel.saveDocs("any")
        val value = cropViewModel.navigationCommands.getOrAwaitValue()
        Truth.assertThat(value.getContentIfNotHandled()).isEqualTo(CropViewModel.NavigationCommand.ToRoot)
    }

    @Test
    fun `throw unknown exception when save doc`() {
        repository.setShouldThrowUnknownException(true)
        cropViewModel.saveDocs("any")
        val value = cropViewModel.cropState.getOrAwaitValue()
        Truth.assertThat((value.getContentIfNotHandled() as CropViewModel.CropState.CropAlertDialog).msg).isEqualTo(
            R.string.common_unknown_error
        )
    }

    @Test
    fun `add photos and navigate to root`() {
        cropViewModel.addPhotos(localId = 1L)
        val value = cropViewModel.navigationCommands.getOrAwaitValue()
        Truth.assertThat(value.getContentIfNotHandled()).isEqualTo(CropViewModel.NavigationCommand.ToRoot)
    }

    @Test
    fun `throw unknown exception when add photos`() {
        repository.setShouldThrowUnknownException(true)
        cropViewModel.addPhotos(localId = -1L)
        val value = cropViewModel.cropState.getOrAwaitValue()
        Truth.assertThat((value.getContentIfNotHandled() as CropViewModel.CropState.CropAlertDialog).msg).isEqualTo(
            R.string.common_unknown_error
        )
    }

    @Test
    fun `save crop photo`() {
        val index = 0
        cropViewModel.setCurrCroppedPosition(index)
        cropViewModel.saveCropPhoto(Uri.EMPTY)
        val value = cropViewModel.listPhoto.getOrAwaitValue()
        val photo = Photo(id = -1L, path = GlobalVariablesTest.fakeFile.path)
        Truth.assertThat(value[index] == photo).isTrue()
    }

    @Test
    fun `throw exception when save crop photo`() {
        GlobalVariablesTest.shouldThrowException = true
        cropViewModel.setCurrCroppedPosition(0)
        cropViewModel.saveCropPhoto(Uri.EMPTY)
        val value = cropViewModel.cropState.getOrAwaitValue()
        Truth.assertThat(
            (value.getContentIfNotHandled() as CropViewModel.CropState.CropAlertDialog).msg
        ).isEqualTo(
            R.string.crop_screen_error_on_save_crop
        )
    }
}