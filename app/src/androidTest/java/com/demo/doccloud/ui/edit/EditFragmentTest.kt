package com.demo.doccloud.ui.edit

import FileUtil.getStubFile
import android.app.Activity
import android.app.Instrumentation
import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import com.demo.doccloud.*
import com.demo.doccloud.domain.entities.Photo
import com.demo.doccloud.domain.usecases.impl.*
import com.demo.doccloud.ui.MainActivity
import com.demo.doccloud.ui.home.HomeFragmentDirections
import com.demo.doccloud.utils.AppConstants
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

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

    @Inject
    lateinit var repository: FakeRepository
    private val mIdlingResource: IdlingResource = EspressoIdlingResource.countingIdlingResource
    private lateinit var context: Context

    @Before
    fun setup() {
        hiltRule.inject()
        context = InstrumentationRegistry.getInstrumentation().targetContext

        val copyFileUseCase = CopyFileImpl(context, Dispatchers.IO)
        val generateDocPdfUseCase = FakeGenerateDocPdfImpl(Dispatchers.Main, context)
        val getDocByIdUseCase = GetDocByIdImpl(repository)
        val fakeScheduleToUpdateRemoteDocName = FakeScheduleToUpdateRemoteDocNameImpl()
        val updateLocalDocName = UpdateLocalDocNameImpl(repository)
        val updatedDocNameUseCase =
            UpdatedDocNameImpl(fakeScheduleToUpdateRemoteDocName, updateLocalDocName)
        val deleteLocalDocPhoto = DeleteLocalDocPhotoImpl(repository)
        val fakeScheduleToDeleteRemoteDocPhoto = FakeScheduleToDeleteRemoteDocPhotoImpl()
        val deleteDocPhotoUseCase =
            DeleteDocPhotoImpl(deleteLocalDocPhoto, fakeScheduleToDeleteRemoteDocPhoto)
        val updateLocalDocPhoto = UpdateLocalDocPhotoImpl(repository)
        val fakeScheduleToUpdateRemoteDocPhoto = FakeScheduleToUpdateRemoteDocPhotoImpl()
        val updateDocPhoto =
            UpdateDocPhotoImpl(updateLocalDocPhoto, fakeScheduleToUpdateRemoteDocPhoto)

        editViewModel = EditViewModel(
            copyFileUseCase,
            generateDocPdfUseCase,
            getDocByIdUseCase,
            updatedDocNameUseCase,
            deleteDocPhotoUseCase,
            updateDocPhoto
        )
        activityScenario = launchFromMainActivityToFragment(
            direction = HomeFragmentDirections.actionHomeFragmentToEditFragment(
                docLocalId = 1L,
                docRemoteId = 1L,
            )
        ).onActivity {
            navController = it.findNavController(R.id.nav_host_fragment)
        }
        Intents.init()
        IdlingRegistry.getInstance().register(mIdlingResource)
        repository.setHasDelay(true)
        GlobalVariablesTest.hasDelay = true
    }

    @After
    fun teardown() {
        IdlingRegistry.getInstance().unregister(mIdlingResource)
        GlobalVariablesTest.clearFlags()
        repository.clearFlags()
        activityScenario.close()
        Intents.release()
    }

    @Test
    fun update_doc_name_with_success() {
        EspressoActions.performClickOnView(R.id.toolbar_title)
        EspressoActions.typeTextOnEditText(R.id.inputName, "new doc name")
        EspressoActions.performClickOnView(R.id.save_btn)
        EspressoActions.checkTextOnTextView(R.id.toolbar_title, "new doc name")
    }

    @Test
    fun throw_exception_when_update_doc_name(){
        repository.setShouldThrowUnknownException(true)
        EspressoActions.performClickOnView(R.id.toolbar_title)
        EspressoActions.typeTextOnEditText(R.id.inputName, "new doc name")
        EspressoActions.performClickOnView(R.id.save_btn)
        EspressoActions.checkTextOnAlertDialog(R.string.common_unknown_error)
    }

    @Test
    fun show_selected_doc(){
        EspressoActions.checkTextOnTextView(R.id.toolbar_title, "doc 1")
    }

    @Test
    fun share_doc_with_success(){
        val intent = Intent()
        intent.putExtra(Intent.EXTRA_STREAM, Uri.EMPTY)
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, intent)
        Intents.intending(IntentMatchers.hasAction(Intent.ACTION_SEND)).respondWith(result)
        EspressoActions.performMenuItemClick(R.string.edit_screen_menu_share, R.id.edit_share)
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
    fun throw_exception_when_share_doc(){
        GlobalVariablesTest.shouldThrowException = true
        val intent = Intent()
        intent.putExtra(Intent.EXTRA_STREAM, Uri.EMPTY)
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, intent)
        Intents.intending(IntentMatchers.hasAction(Intent.ACTION_SEND)).respondWith(result)
        EspressoActions.performMenuItemClick(R.string.edit_screen_menu_share, R.id.edit_share)
        EspressoActions.checkTextOnAlertDialog(R.string.home_alert_error_generate_pdf)
    }

    @Test
    fun pick_single_img_from_gallery_and_navigate(){
        val intent = Intent()
        intent.data = Uri.fromFile(getStubFile(context))
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, intent)
        Intents.intending(IntentMatchers.hasAction(Intent.ACTION_OPEN_DOCUMENT)).respondWith(result)
        EspressoActions.performClickOnView(R.id.fab)
        EspressoActions.performClickOnView(R.id.galleryTv)
        ViewMatchers.assertThat(
            navController.currentDestination?.id,
            Matchers.`is`(R.id.cropFragment)
        )
    }

    @Test
    fun pick_many_img_from_gallery_and_navigate() {
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
        EspressoActions.performClickOnView(R.id.fab)
        EspressoActions.performClickOnView(R.id.galleryTv)
        ViewMatchers.assertThat(
            navController.currentDestination?.id,
            Matchers.`is`(R.id.cropFragment)
        )
    }

    @Test
    fun throw_exception_when_pick_imgs_from_gallery(){
        GlobalVariablesTest.shouldThrowException = true
        val intent = Intent()
        intent.data = Uri.EMPTY
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, intent)
        Intents.intending(IntentMatchers.hasAction(Intent.ACTION_OPEN_DOCUMENT)).respondWith(result)
        EspressoActions.performClickOnView(R.id.fab)
        EspressoActions.performClickOnView(R.id.galleryTv)
        EspressoActions.checkTextOnAlertDialog(R.string.home_alert_error_copy_image_from_gallery)
    }

    @Test
    fun delete_single_photo(){
        activityScenario.onActivity {
            val value = editViewModel.doc.getOrAwaitValue()
            ViewMatchers.assertThat(
                value.pages.size,
                Matchers.`is`(1)
            )
        }
        EspressoActions.performClickOnRecyclerViewItem(R.id.rv_doc_photos, 0)
        Espresso.openContextualActionModeOverflowMenu()
        EspressoActions.performMenuItemClick(R.string.common_delete_label, R.id.edit_delete)
        EspressoActions.performClickOnText(R.string.alert_dialog_yes_button)
        ViewMatchers.assertThat(
            navController.currentDestination?.id,
            Matchers.`is`(R.id.editFragment)
        )
        activityScenario.onActivity {
            val value = editViewModel.doc.getOrAwaitValue()
            ViewMatchers.assertThat(
                value.pages.size,
                Matchers.`is`(0)
            )
        }
    }

    @Test
    fun throw_unknown_exception_when_delete_doc_photo(){
        repository.setShouldThrowUnknownException(true)
        EspressoActions.performClickOnRecyclerViewItem(R.id.rv_doc_photos, 0)
        Espresso.openContextualActionModeOverflowMenu()
        EspressoActions.performMenuItemClick(R.string.common_delete_label, R.id.edit_delete)
        EspressoActions.performClickOnText(R.string.alert_dialog_yes_button)
        EspressoActions.checkTextOnAlertDialog(R.string.common_unknown_error)
    }

    @Test
    fun crop_real_photo(){
        activityScenario.onActivity {
            val doc = editViewModel.doc.getOrAwaitValue()
            editViewModel.doc.value = doc.copy(pages = listOf(Photo(id = 1, path = getStubFile(context).path )))
        }
        EspressoActions.performClickOnRecyclerViewItem(R.id.rv_doc_photos, 0)
        EspressoActions.performMenuItemClick(R.string.edit_screen_menu_crop, R.id.edit_crop)
        EspressoActions.performMenuItemClick(R.string.crop_image_menu_crop, R.id.crop_image_menu_crop)
    }

    @Test
    fun throw_unknown_exception_when_crop_doc_photo(){
        repository.setShouldThrowUnknownException(true)
        activityScenario.onActivity {
            val doc = editViewModel.doc.getOrAwaitValue()
            editViewModel.doc.value = doc.copy(pages = listOf(Photo(id = 1, path = getStubFile(context).path )))
        }
        EspressoActions.performClickOnRecyclerViewItem(R.id.rv_doc_photos, 0)
        EspressoActions.performMenuItemClick(R.string.edit_screen_menu_crop, R.id.edit_crop)
        EspressoActions.performMenuItemClick(R.string.crop_image_menu_crop, R.id.crop_image_menu_crop)
        EspressoActions.checkTextOnAlertDialog(R.string.common_unknown_error)
    }
}
