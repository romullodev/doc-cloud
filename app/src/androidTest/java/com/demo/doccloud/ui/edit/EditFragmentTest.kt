package com.demo.doccloud.ui.edit

import FileUtil.getStubFile
import android.app.Activity
import android.app.Instrumentation
import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.BundleMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.demo.doccloud.*
import com.demo.doccloud.data.datasource.local.LocalDataSource
import com.demo.doccloud.data.datasource.remote.RemoteDataSource
import com.demo.doccloud.data.repository.RepositoryImpl
import com.demo.doccloud.domain.usecases.impl.*
import com.demo.doccloud.ui.AndroidTestUtil
import com.demo.doccloud.ui.MainActivity
import com.demo.doccloud.ui.home.HomeFragmentDirections
import com.demo.doccloud.utils.AppConstants
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.lang.RuntimeException

@HiltAndroidTest
@MediumTest
@ExperimentalCoroutinesApi
class EditFragmentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var activityScenario: ActivityScenario<MainActivity>
    private lateinit var navController: NavController

    @BindValue
    lateinit var editViewModel: EditViewModel

    @BindValue
    lateinit var repository: RepositoryImpl
    private lateinit var mIdlingResource: IdlingResource
    private lateinit var context: Context
    private var mockLocalDataSource: LocalDataSource = mockk()
    private var mockRemoteDataSource: RemoteDataSource = mockk()

    @Before
    fun setup() {
        hiltRule.inject()
        context = InstrumentationRegistry.getInstrumentation().targetContext
        repository = RepositoryImpl(mockRemoteDataSource, mockLocalDataSource, context)
        val dispatcher = Dispatchers.Default

        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()

        // Initialize WorkManager for instrumentation tests.
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)

        mIdlingResource = EspressoIdlingResource.countingIdlingResource
        val copyFileUseCase = FakeCopyFileImpl()
        val generateDocPdfUseCase = FakeGenerateDocPdfImpl(dispatcher, context)
        val getDocByIdUseCase = GetDocByIdImpl(repository)
        val scheduleToUpdateRemoteDocName = ScheduleToUpdateRemoteDocNameImpl(context)
        val updateLocalDocName = UpdateLocalDocNameImpl(repository)
        val updatedDocNameUseCase =
            UpdatedDocNameImpl(scheduleToUpdateRemoteDocName, updateLocalDocName)
        val deleteLocalDocPhoto = DeleteLocalDocPhotoImpl(repository)
        val scheduleToDeleteRemoteDocPhoto = ScheduleToDeleteRemoteDocPhotoImpl(context)
        val deleteDocPhotoUseCase =
            DeleteDocPhotoImpl(deleteLocalDocPhoto, scheduleToDeleteRemoteDocPhoto)
        val updateLocalDocPhoto = UpdateLocalDocPhotoImpl(repository)
        val scheduleToUpdateRemoteDocPhoto = ScheduleToUpdateRemoteDocPhotoImpl(context)
        val updateDocPhoto = UpdateDocPhotoImpl(updateLocalDocPhoto, scheduleToUpdateRemoteDocPhoto)

        editViewModel = EditViewModel(
            copyFileUseCase,
            generateDocPdfUseCase,
            getDocByIdUseCase,
            updatedDocNameUseCase,
            deleteDocPhotoUseCase,
            updateDocPhoto
        )
        coEvery { mockRemoteDataSource.getUser() } returns AndroidTestUtil.getUser()
        coEvery { mockLocalDataSource.getSavedDocs() } returns MutableLiveData(
            AndroidTestUtil.getRealDocs(
                context
            )
        )
        Intents.init()
        IdlingRegistry.getInstance().register(mIdlingResource)
    }

    @After
    fun teardown() {
        IdlingRegistry.getInstance().unregister(mIdlingResource)
        activityScenario.close()
        GlobalVariablesTest.clearFlags()
        Intents.release()
    }

    private fun launchFragment() {
        activityScenario = launchFromMainActivityToFragment(
            direction = HomeFragmentDirections.actionHomeFragmentToEditFragment(
                docLocalId = 1L,
                docRemoteId = 1L,
            )
        ).onActivity {
            navController = it.findNavController(R.id.nav_host_fragment)
        }
    }

    @Test
    fun update_doc_name_with_success() {
        //Arrange
        coEvery { mockLocalDataSource.getDoc(any()) } returns AndroidTestUtil.getRealDoc(context)
        coEvery { mockLocalDataSource.updateDocName(any(), any()) } returns mockk()
        launchFragment()

        //Act
        EspressoActions.performClickOnView(R.id.toolbar_title)
        EspressoActions.typeTextOnEditText(R.id.inputName, "new doc name")
        EspressoActions.performClickOnView(R.id.save_btn)

        //Assert
        EspressoActions.checkTextOnTextView(R.id.toolbar_title, "new doc name")
    }

    @Test
    fun throw_exception_when_update_doc_name() {
        //Arrange
        coEvery { mockLocalDataSource.getDoc(any()) } returns AndroidTestUtil.getRealDoc(context)
        coEvery { mockLocalDataSource.updateDocName(any(), any()) } throws (RuntimeException())
        launchFragment()

        //Act
        EspressoActions.performClickOnView(R.id.toolbar_title)
        EspressoActions.typeTextOnEditText(R.id.inputName, "new doc name")
        EspressoActions.performClickOnView(R.id.save_btn)

        //Assert
        EspressoActions.checkTextOnAlertDialog(R.string.common_unknown_error)
    }

    @Test
    fun show_selected_doc() {
        //Arrange
        val doc = AndroidTestUtil.getRealDoc(context)
        coEvery { mockLocalDataSource.getDoc(any()) } returns doc

        //Act
        launchFragment()

        //Assert
        EspressoActions.checkTextOnTextView(R.id.toolbar_title, doc.name)
    }

    @Test
    fun share_doc_with_success() {
        //Arrange
        val intent = Intent()
        intent.putExtra(Intent.EXTRA_STREAM, Uri.EMPTY)
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, intent)
        Intents.intending(IntentMatchers.hasAction(Intent.ACTION_SEND)).respondWith(result)
        coEvery { mockLocalDataSource.getDoc(any()) } returns AndroidTestUtil.getRealDoc(context)
        launchFragment()

        //Act
        EspressoActions.performMenuItemClick(R.string.edit_screen_menu_share, R.id.edit_share)

        //Assert
        Intents.intended(
            CoreMatchers.allOf(
                IntentMatchers.hasExtras(
                    CoreMatchers.allOf(
                        BundleMatchers.hasEntry(
                            Matchers.equalTo(Intent.EXTRA_INTENT),
                            IntentMatchers.hasAction(Intent.ACTION_SEND)
                        ),
                        BundleMatchers.hasEntry(
                            Matchers.equalTo(Intent.EXTRA_INTENT),
                            IntentMatchers.hasType(AppConstants.INTENT_PDF_TYPE)
                        ),
                        BundleMatchers.hasEntry(
                            Matchers.equalTo(Intent.EXTRA_TITLE),
                            CoreMatchers.containsString(context.getString(R.string.common_share_with))
                        )
                    )
                ),
                IntentMatchers.hasAction(Matchers.equalTo(Intent.ACTION_CHOOSER))
            )
        )
    }

    @Test
    fun throw_exception_when_share_doc() {
        //Arrange
        GlobalVariablesTest.shouldThrowException = true
        val intent = Intent()
        intent.putExtra(Intent.EXTRA_STREAM, Uri.EMPTY)
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, intent)
        Intents.intending(IntentMatchers.hasAction(Intent.ACTION_SEND)).respondWith(result)
        coEvery { mockLocalDataSource.getDoc(any()) } returns AndroidTestUtil.getRealDoc(context)
        launchFragment()

        //Act
        EspressoActions.performMenuItemClick(R.string.edit_screen_menu_share, R.id.edit_share)

        //Assert
        EspressoActions.checkTextOnAlertDialog(R.string.home_alert_error_generate_pdf)
    }

    @Test
    fun pick_single_img_from_gallery_and_navigate() {
        //Arrange
        val intent = Intent()
        intent.data = Uri.fromFile(getStubFile(context))
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, intent)
        Intents.intending(IntentMatchers.hasAction(Intent.ACTION_OPEN_DOCUMENT)).respondWith(result)
        coEvery { mockLocalDataSource.getDoc(any()) } returns AndroidTestUtil.getRealDoc(context)
        launchFragment()

        //Act
        EspressoActions.performClickOnView(R.id.fab)
        EspressoActions.performClickOnView(R.id.galleryTv)

        //Assert
        ViewMatchers.assertThat(
            navController.currentDestination?.id,
            Matchers.`is`(R.id.cropFragment)
        )
    }

    @Test
    fun pick_many_img_from_gallery_and_navigate() {
        //Arrange
        val intent = Intent()
        val clipDescription = ClipDescription("Dummy", arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN))
        val uri = Uri.fromFile(getStubFile(context))
        val clipItem = ClipData.Item(uri)
        intent.clipData = ClipData(clipDescription, clipItem)
        intent.clipData!!.addItem(clipItem)
        intent.clipData!!.addItem(clipItem)
        intent.clipData!!.addItem(clipItem)
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, intent)
        Intents.intending(IntentMatchers.hasAction(Intent.ACTION_OPEN_DOCUMENT)).respondWith(result)
        coEvery { mockLocalDataSource.getDoc(any()) } returns AndroidTestUtil.getRealDoc(context)
        launchFragment()

        //Act
        EspressoActions.performClickOnView(R.id.fab)
        EspressoActions.performClickOnView(R.id.galleryTv)

        //Assert
        ViewMatchers.assertThat(
            navController.currentDestination?.id,
            Matchers.`is`(R.id.cropFragment)
        )
    }

    @Test
    fun throw_exception_when_pick_imgs_from_gallery() {
        //Arrange
        GlobalVariablesTest.shouldThrowException = true
        val intent = Intent()
        intent.data = Uri.EMPTY
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, intent)
        Intents.intending(IntentMatchers.hasAction(Intent.ACTION_OPEN_DOCUMENT)).respondWith(result)
        coEvery { mockLocalDataSource.getDoc(any()) } returns AndroidTestUtil.getRealDoc(context)
        launchFragment()

        //Act
        EspressoActions.performClickOnView(R.id.fab)
        EspressoActions.performClickOnView(R.id.galleryTv)

        //Assert
        EspressoActions.checkTextOnAlertDialog(R.string.home_alert_error_copy_image_from_gallery)
    }

//    @Test
//    fun delete_single_photo() {
//        //Arrange
//        coEvery { mockLocalDataSource.getDoc(any()) } returns AndroidTestUtil.getRealDoc(context)
//        launchFragment()
//        activityScenario.onActivity {
//            val value = editViewModel.doc.getOrAwaitValue()
//            ViewMatchers.assertThat(
//                value.pages.size,
//                Matchers.`is`(1)
//            )
//        }
//        EspressoActions.performClickOnRecyclerViewItem(R.id.rv_doc_photos, 0)
//        Espresso.openContextualActionModeOverflowMenu()
//        EspressoActions.performMenuItemClick(R.string.common_delete_label, R.id.edit_delete)
//        EspressoActions.performClickOnText(R.string.alert_dialog_yes_button)
//        ViewMatchers.assertThat(
//            navController.currentDestination?.id,
//            Matchers.`is`(R.id.editFragment)
//        )
//        activityScenario.onActivity {
//            val value = editViewModel.doc.getOrAwaitValue()
//            ViewMatchers.assertThat(
//                value.pages.size,
//                Matchers.`is`(0)
//            )
//        }
//    }
//}

    @Test
    fun throw_unknown_exception_when_delete_doc_photo() {
        //Arrange
        coEvery { mockLocalDataSource.getDoc(any()) } returns AndroidTestUtil.getRealDoc(context)
        coEvery { mockLocalDataSource.deleteDocPhoto(any(), any()) } throws (RuntimeException())
        launchFragment()

        //Act
        EspressoActions.performClickOnRecyclerViewItem(R.id.rv_doc_photos, 0)
        Espresso.openContextualActionModeOverflowMenu()
        EspressoActions.performMenuItemClick(R.string.common_delete_label, R.id.edit_delete)
        EspressoActions.performClickOnText(R.string.alert_dialog_yes_button)

        //Assert
        EspressoActions.checkTextOnAlertDialog(R.string.common_unknown_error)
    }

    @Test
    fun crop_real_photo() {
        //Arrange
        coEvery { mockLocalDataSource.getDoc(any()) } returns AndroidTestUtil.getRealDoc(context)
        coEvery { mockLocalDataSource.updateDocPhoto(any(), any()) } returns mockk()
        launchFragment()
//        activityScenario.onActivity {
//            val doc = editViewModel.doc.getOrAwaitValue()
//            editViewModel.doc.value =
//                doc.copy(pages = listOf(Photo(id = 1, path = getStubFile(context).path)))
//        }

        //Act
        EspressoActions.performClickOnRecyclerViewItem(R.id.rv_doc_photos, 0)
        EspressoActions.performMenuItemClick(R.string.edit_screen_menu_crop, R.id.edit_crop)
        EspressoActions.performMenuItemClick(
            R.string.crop_image_menu_crop,
            R.id.crop_image_menu_crop
        )
    }

    @Test
    fun throw_unknown_exception_when_crop_doc_photo() {
        //Arrange
        coEvery { mockLocalDataSource.getDoc(any()) } returns AndroidTestUtil.getRealDoc(context)
        coEvery { mockLocalDataSource.updateDocPhoto(any(), any()) } throws RuntimeException()
        launchFragment()
//        activityScenario.onActivity {
//            val doc = editViewModel.doc.getOrAwaitValue()
//            editViewModel.doc.value =
//                doc.copy(pages = listOf(Photo(id = 1, path = getStubFile(context).path)))
//        }

        //Act
        EspressoActions.performClickOnRecyclerViewItem(R.id.rv_doc_photos, 0)
        EspressoActions.performMenuItemClick(R.string.edit_screen_menu_crop, R.id.edit_crop)
        EspressoActions.performMenuItemClick(
            R.string.crop_image_menu_crop,
            R.id.crop_image_menu_crop
        )

        //Assert
        EspressoActions.checkTextOnAlertDialog(R.string.common_unknown_error)
    }
}