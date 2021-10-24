package com.demo.doccloud.ui.crop

import FileUtil.getStubFile
import android.app.Activity
import android.app.Instrumentation
import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.demo.doccloud.*
import com.demo.doccloud.data.datasource.local.AppLocalServices
import com.demo.doccloud.data.datasource.remote.RemoteDataSource
import com.demo.doccloud.data.repository.RepositoryImpl
import com.demo.doccloud.domain.entities.Photo
import com.demo.doccloud.idling.EspressoIdlingResource
import com.demo.doccloud.ui.AndroidTestUtil
import com.demo.doccloud.ui.MainActivity
import com.demo.doccloud.ui.home.HomeFragmentDirections
import com.demo.doccloud.utils.BackToRoot
import com.demo.doccloud.utils.ListPhotoArg
import com.demo.doccloud.utils.RootDestination
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

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

    @BindValue
    lateinit var repository: RepositoryImpl
    private lateinit var mIdlingResource: IdlingResource
    private lateinit var context: Context
    private lateinit var localDataSource: AppLocalServices
    private var mockRemoteDataSource: RemoteDataSource = mockk()
    private var localId = 0L

    @Before
    fun setup(): Unit = runBlocking {
        hiltRule.inject()
        context = InstrumentationRegistry.getInstrumentation().targetContext
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()
        // Initialize WorkManager for instrumentation tests.
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
        localDataSource = AndroidTestUtil.getLocalDataSource(context)
        repository = RepositoryImpl(mockRemoteDataSource, localDataSource, context)
        coEvery { mockRemoteDataSource.getUser() } returns AndroidTestUtil.getUser()
        localDataSource.clearAllData()
        localId = localDataSource.saveDocOnDevice(AndroidTestUtil.getRealDoc(context))
        cropViewModel = AndroidTestUtil.getCropViewModel(context, repository)
        mIdlingResource = EspressoIdlingResource.countingIdlingResource
        IdlingRegistry.getInstance().register(mIdlingResource)
        Intents.init()
    }

    @After
    fun teardown() {
        IdlingRegistry.getInstance().unregister(mIdlingResource)
        GlobalVariablesTest.clearFlags()
        activityScenario.close()
        Intents.release()
    }

    private fun launchFragment(root: RootDestination = RootDestination.HOME_DESTINATION) {
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
                    rootDestination = root
                )
            )
        ).onActivity {
            navController = it.findNavController(R.id.nav_host_fragment)
        }
    }

    @Test
    fun save_doc_navigate_to_home() {
        //Arrange
        launchFragment()

        //Act
        EspressoActions.performClickOnView(R.id.continue_btn)
        EspressoActions.typeTextOnEditText(R.id.inputName, "doc 3")
        EspressoActions.performClickOnView(R.id.save_btn)

        //Assert
        ViewMatchers.assertThat(
            navController.currentDestination?.id,
            Matchers.`is`(R.id.homeFragment)
        )
    }

    @Test
    fun save_doc_navigate_to_edit() {
        //Arrange
        activityScenario = launchMyMainActivity().onActivity {
            navController = it.findNavController(R.id.nav_host_fragment)
        }
        EspressoActions.performClickOnRecyclerViewItem(R.id.rvHome, 0)
        val intent = Intent()
        val clipDescription =
            ClipDescription("DummyLabel", arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN))
        val uri = Uri.fromFile(getStubFile(context))
        val clipItem = ClipData.Item(uri)
        intent.clipData = ClipData(clipDescription, clipItem)
        intent.clipData!!.addItem(clipItem)
        intent.clipData!!.addItem(clipItem)
        intent.clipData!!.addItem(clipItem)
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, intent)
        Intents.intending(IntentMatchers.hasAction(Intent.ACTION_OPEN_DOCUMENT)).respondWith(result)

        //Act
        EspressoActions.performClickOnView(R.id.fab)
        EspressoActions.performClickOnView(R.id.galleryTv)
        EspressoActions.performClickOnView(R.id.continue_btn)

        //Assert
        ViewMatchers.assertThat(
            navController.currentDestination?.id,
            Matchers.`is`(R.id.editFragment)
        )
    }


    @Test
    fun check_continue_button_from_edit() {
        launchFragment(RootDestination.EDIT_DESTINATION)
        EspressoActions.checkTextOnButton(
            R.id.continue_btn,
            context.getString(R.string.crop_screen_add_label)
        )
    }

    @Test
    fun check_add_button_from_home(){
        launchFragment()
        EspressoActions.checkTextOnButton(R.id.continue_btn, context.getString(R.string.crop_screen_continue_label))
    }
}