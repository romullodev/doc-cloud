package com.demo.doccloud.ui.crop

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
import androidx.room.util.FileUtil
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.demo.doccloud.*
import com.demo.doccloud.domain.entities.Photo
import com.demo.doccloud.domain.usecases.impl.*
import com.demo.doccloud.ui.MainActivity
import com.demo.doccloud.ui.edit.EditViewModel
import com.demo.doccloud.ui.home.HomeFragmentDirections
import com.demo.doccloud.utils.BackToRoot
import com.demo.doccloud.utils.ListPhotoArg
import com.demo.doccloud.utils.RootDestination
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
@MediumTest
@ExperimentalCoroutinesApi
class CropFragmentTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var activityScenario: ActivityScenario<MainActivity>
    private lateinit var navController: NavController

    @BindValue
    lateinit var cropViewModel: CropViewModel

    @Inject
    lateinit var repository: FakeRepository
    private val mIdlingResource: IdlingResource = EspressoIdlingResource.countingIdlingResource
    private lateinit var context: Context

    @Before
    fun setup() {
        hiltRule.inject()
        context = InstrumentationRegistry.getInstrumentation().targetContext
        val saveLocalDoc = SaveLocalDocImpl(repository)
        val fakeScheduleToSaveRemoteDocImpl = FakeScheduleToSaveRemoteDocImpl()
        val saveDocUseCase = SaveDocImpl(saveLocalDoc, fakeScheduleToSaveRemoteDocImpl)

        val addPhotosToLocalDoc = AddPhotosToLocalDocImpl(repository)
        val fakeScheduleToAddRemoteDocPhotos = FakeScheduleToAddRemoteDocPhotosImpl()
        val addPhotosUseCase = AddPhotosImpl(addPhotosToLocalDoc, fakeScheduleToAddRemoteDocPhotos)

        val copyFileUseCase = CopyFileImpl(context, Dispatchers.Main)

        cropViewModel = CropViewModel(
            saveDocUseCase,
            addPhotosUseCase,
            copyFileUseCase
        )
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
    fun save_doc_navigate_to_home() {
        activityScenario = launchFromMainActivityToFragment(
            direction = HomeFragmentDirections.actionHomeFragmentToCropFragment(
                photos = ListPhotoArg(
                    arrayListOf(
                        Photo(
                            id = 1L,
                            path = getStubFile(context).path
                        )
                    )
                ),
                root = BackToRoot(
                    rootDestination = RootDestination.HOME_DESTINATION
                )
            )
        ).onActivity {
            navController = it.findNavController(R.id.nav_host_fragment)
        }

        EspressoActions.performClickOnView(R.id.continue_btn)
        EspressoActions.typeTextOnEditText(R.id.inputName, "doc 3")
        EspressoActions.performClickOnView(R.id.save_btn)
        ViewMatchers.assertThat(
            navController.currentDestination?.id,
            Matchers.`is`(R.id.homeFragment)
        )
        EspressoActions.checkSizeOnRecyclerView(R.id.rvHome, 3)
    }

    @Test
    fun save_doc_navigate_to_edit() {
        activityScenario = launchMyMainActivity().onActivity {
            navController = it.findNavController(R.id.nav_host_fragment)
        }
        EspressoActions.performClickOnRecyclerViewItem(R.id.rvHome, 0)
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
        EspressoActions.performClickOnView(R.id.continue_btn)
        ViewMatchers.assertThat(
            navController.currentDestination?.id,
            Matchers.`is`(R.id.editFragment)
        )
    }

    @Test
    fun check_continue_button_from_edit(){
        activityScenario = launchFromMainActivityToFragment(
            direction = HomeFragmentDirections.actionHomeFragmentToCropFragment(
                photos = ListPhotoArg(
                    arrayListOf(
                        Photo(
                            id = 1L,
                            path = getStubFile(context).path
                        )
                    )
                ),
                root = BackToRoot(
                    rootDestination = RootDestination.EDIT_DESTINATION,
                    localId = 1L
                ),
            )
        ).onActivity {
            navController = it.findNavController(R.id.nav_host_fragment)
        }
        EspressoActions.checkTextOnButton(R.id.continue_btn, context.getString(R.string.crop_screen_add_label))
    }

    @Test
    fun check_add_button_from_home(){
        activityScenario = launchFromMainActivityToFragment(
            direction = HomeFragmentDirections.actionHomeFragmentToCropFragment(
                photos = ListPhotoArg(
                    arrayListOf(
                        Photo(
                            id = 1L,
                            path = getStubFile(context).path
                        )
                    )
                ),
                root = BackToRoot(
                    rootDestination = RootDestination.HOME_DESTINATION
                )
            )
        ).onActivity {
            navController = it.findNavController(R.id.nav_host_fragment)
        }
        EspressoActions.checkTextOnButton(R.id.continue_btn, context.getString(R.string.crop_screen_continue_label))
    }
}