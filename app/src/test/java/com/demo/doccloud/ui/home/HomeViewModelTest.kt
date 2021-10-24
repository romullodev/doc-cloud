package com.demo.doccloud.ui.home

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.demo.doccloud.*
import com.demo.doccloud.FakeRepository
import com.demo.doccloud.domain.entities.Doc
import com.demo.doccloud.domain.entities.DocStatus
import com.demo.doccloud.domain.usecases.impl.*
import com.demo.doccloud.utils.BackToRoot
import com.demo.doccloud.utils.ListPhotoArg
import com.demo.doccloud.utils.RootDestination
import com.google.common.truth.Truth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
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
class HomeViewModelTest{

    private lateinit var repository: FakeRepository
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var doc: Doc
    private lateinit var context: Context

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        repository = FakeRepository(context)

        //val copyFileUseCase = CopyFileImpl(context, Dispatchers.Main)
        val copyFileUseCase = FakeCopyFileImpl()
        val fakeGenerateDocPdfUseCase = FakeGenerateDocPdfImpl(Dispatchers.Main, context)
        val doLogoutUseCase = DoLogoutImpl(repository)
        val fakeScheduleToDeleteRemoteDoc = FakeScheduleToDeleteRemoteDocImpl()
        val deleteLocalDoc = DeleteLocalDocImpl(repository)
        val deleteDocUseCase = DeleteDocImpl(deleteLocalDoc, fakeScheduleToDeleteRemoteDoc, context)
        val getUserUseCase = GetUserImpl(repository)
        val fakeScheduleToSyncData = FakeScheduleToSyncDataImpl()
        val getAllDocsUse = GetAllDocsImpl(repository)

        homeViewModel = HomeViewModel(
            copyFileUseCase,
            fakeGenerateDocPdfUseCase,
            doLogoutUseCase,
            deleteDocUseCase,
            getUserUseCase,
            fakeScheduleToSyncData,
            getAllDocsUse
        )

        doc = Doc(
            remoteId = -1L,
            name = "any name",
            date = "",
            pages = listOf(),
            status = DocStatus.NOT_SENT,
        )
    }

    @After
    fun teardown(){
        GlobalVariablesTest.clearFlags()
        repository.clearFlags()
    }

    @Test
    fun `do logout with success`() = mainCoroutineRule.runBlockingTest{
        homeViewModel.doLogout()
        val value = homeViewModel.navigationCommands.getOrAwaitValue()
        Truth.assertThat((value.getContentIfNotHandled() as HomeViewModel.NavigationCommand.To).directions)
            .isEqualTo(
                HomeFragmentDirections.actionHomeFragmentToLoginFragment()
            )
    }

    @Test
    fun `share doc successfully`() = mainCoroutineRule.runBlockingTest{
        homeViewModel.currDoc = doc
        homeViewModel.shareDoc()
        val value = homeViewModel.homeState.getOrAwaitValue()
        Truth.assertThat((value.getContentIfNotHandled() as HomeViewModel.HomeState.SharePdf).data.isFile)
    }

    @Test

    fun `share doc with exception`() = mainCoroutineRule.runBlockingTest{
        GlobalVariablesTest.shouldThrowException = true
        homeViewModel.currDoc = doc
        homeViewModel.shareDoc()
        val value = homeViewModel.homeState.getOrAwaitValue()
        Truth.assertThat((value.getContentIfNotHandled() as HomeViewModel.HomeState.HomeAlertDialog).msg).isEqualTo(
            R.string.home_alert_error_generate_pdf
        )
    }


    @Test
    fun `delete doc`() = mainCoroutineRule.runBlockingTest{
        homeViewModel.currDoc = doc
        homeViewModel.deleteDoc()
        val value = homeViewModel.homeState.getOrAwaitValue()
        Truth.assertThat((value.getContentIfNotHandled() as HomeViewModel.HomeState.HomeToastMessage).msg).isEqualTo(
            context.getString(R.string.home_toast_delete_success, doc.name)
        )
    }

    @Test
    fun `throw exception when delete doc`() = mainCoroutineRule.runBlockingTest{
        repository.setShouldThrowExceptionWhenDeleteLocalDoc(true)
        homeViewModel.currDoc = doc
        homeViewModel.deleteDoc()
        val value = homeViewModel.homeState.getOrAwaitValue()
        Truth.assertThat((value.getContentIfNotHandled() as HomeViewModel.HomeState.HomeAlertDialog).msg).isEqualTo(
            R.string.home_toast_delete_error
        )
    }

    @Test
    fun `copy and navigate to Crop Fragment`() = mainCoroutineRule.runBlockingTest{
        homeViewModel.copyAndNavigateToCrop(listOf())
        val value = homeViewModel.navigationCommands.getOrAwaitValue()
        Truth.assertThat((value.getContentIfNotHandled() as HomeViewModel.NavigationCommand.To).directions).isEqualTo(
            HomeFragmentDirections.actionHomeFragmentToCropFragment(
                photos = ListPhotoArg(ArrayList()),
                root = BackToRoot(rootDestination = RootDestination.HOME_DESTINATION)
            )
        )
    }

    @Test
    fun `throw exception when copy and navigate do Crop Fragment`() = mainCoroutineRule.runBlockingTest{
        GlobalVariablesTest.shouldThrowException = true
        homeViewModel.copyAndNavigateToCrop(listOf(Uri.EMPTY))
        val value = homeViewModel.homeState.getOrAwaitValue()
        Truth.assertThat((value.getContentIfNotHandled() as HomeViewModel.HomeState.HomeAlertDialog).msg).isEqualTo(
            R.string.home_alert_error_copy_image_from_gallery
        )
    }


    @Test
    fun `move user to login page`() = mainCoroutineRule.runBlockingTest{
        repository.setShouldThrowExceptionWhenGetUser(true)
        homeViewModel.setupInitVariables()
        val value = homeViewModel.navigationCommands.getOrAwaitValue()
        Truth.assertThat((value.getContentIfNotHandled() as HomeViewModel.NavigationCommand.To).directions).isEqualTo(
            HomeFragmentDirections.actionHomeFragmentToLoginFragment()
        )
    }

    @Test
    fun `get docs`() = mainCoroutineRule.runBlockingTest{
        val value = homeViewModel.docs.getOrAwaitValue()
        Truth.assertThat(value).isNotNull()
    }
}