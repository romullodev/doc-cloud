package com.demo.doccloud.ui.home

import android.app.Activity
import android.app.Instrumentation
import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_OPEN_DOCUMENT
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.openContextualActionModeOverflowMenu
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.BundleMatchers.hasEntry
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.demo.doccloud.*
import com.demo.doccloud.data.datasource.local.LocalDataSource
import com.demo.doccloud.data.datasource.remote.RemoteDataSource
import com.demo.doccloud.data.repository.RepositoryImpl
import com.demo.doccloud.ui.AndroidTestUtil
import com.demo.doccloud.ui.AndroidTestUtil.Companion.getRealDocs
import com.demo.doccloud.ui.MainActivity
import com.demo.doccloud.utils.AppConstants
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matchers
import org.hamcrest.Matchers.containsString
import org.hamcrest.core.AllOf.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@HiltAndroidTest
@MediumTest
@ExperimentalCoroutinesApi
class HomeFragmentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var activityScenario: ActivityScenario<MainActivity>
    private lateinit var navController: NavController

    @BindValue
    lateinit var homeViewModel: HomeViewModel

    @BindValue
    lateinit var repository: RepositoryImpl
    private lateinit var mIdlingResource: IdlingResource
    private lateinit var context: Context
    private var localDataSource: LocalDataSource = mockk()
    private var remoteDataSource: RemoteDataSource = mockk()

    @Before
    fun setup() {
        hiltRule.inject()
        context = InstrumentationRegistry.getInstrumentation().targetContext
        repository = RepositoryImpl(remoteDataSource, localDataSource, context)
        mIdlingResource = EspressoIdlingResource.countingIdlingResource

        coEvery { localDataSource.getSavedDocs() } returns MutableLiveData(
            AndroidTestUtil.getRealDocs(
                context
            )
        )
        coEvery { remoteDataSource.getUser() } returns AndroidTestUtil.getUser()

        homeViewModel = AndroidTestUtil.getHomeViewModel(context, repository)

        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()
        // Initialize WorkManager for instrumentation tests.
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
        Intents.init()
        IdlingRegistry.getInstance().register(mIdlingResource)
    }

    @After
    fun teardown() {
        IdlingRegistry.getInstance().unregister(mIdlingResource)
        activityScenario.close()
        Intents.release()
    }

    @Test
    fun do_logout_successfully() {
        //Arrange
        coEvery { localDataSource.clearAllData() } returns mockk() //coAnswers { wrapEspressoIdlingResource {} }
        coEvery { remoteDataSource.doLogout() } returns mockk()//coAnswers { wrapEspressoIdlingResource {} }
        activityScenario = launchMyMainActivity().onActivity {
            navController = it.findNavController(R.id.nav_host_fragment)
        }

        //Act
        openContextualActionModeOverflowMenu()
        EspressoActions.performMenuItemClick(R.string.home_menu_logout, R.id.logout)

        //Assert
        assertThat(navController.currentDestination?.id, Matchers.`is`(R.id.loginFragment))
    }

    @Test
    fun share_doc_successfully() {
        //Arrange
        val intent = Intent()
        intent.putExtra(Intent.EXTRA_STREAM, Uri.EMPTY)
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, intent)
        intending(hasAction(Intent.ACTION_SEND)).respondWith(result)
        activityScenario = launchMyMainActivity().onActivity {
            navController = it.findNavController(R.id.nav_host_fragment)
        }

        //Act
        EspressoActions.performLongClickOnRecyclerViewItem(R.id.rvHome, 0)
        EspressoActions.performMenuItemClick(R.string.home_doc_item_share, R.id.share)

        //Assert
        intended(
            allOf(
                hasExtras(
                    allOf(
                        hasEntry(equalTo(Intent.EXTRA_INTENT), hasAction(Intent.ACTION_SEND)),
                        hasEntry(
                            equalTo(Intent.EXTRA_INTENT),
                            hasType(AppConstants.INTENT_PDF_TYPE)
                        ),
                        hasEntry(
                            equalTo(Intent.EXTRA_TITLE),
                            containsString(context.getString(R.string.common_share_with))
                        )
                    )
                ),
                hasAction(equalTo(Intent.ACTION_CHOOSER))
            )
        )
    }

    @Test
    fun share_doc_with_exception() {
        //Arrange
        //workaround to throw an exception on this use case
        val fakeGenerateDocPdf = FakeGenerateDocPdfImpl(Dispatchers.Default, context)
        GlobalVariablesTest.shouldThrowException = true
        homeViewModel = AndroidTestUtil.getHomeViewModelWithMockGeneratePdfUseCase(
            context,
            repository,
            fakeGenerateDocPdf
        )
        activityScenario = launchMyMainActivity().onActivity {
            navController = it.findNavController(R.id.nav_host_fragment)
        }

        //Act
        EspressoActions.performLongClickOnRecyclerViewItem(R.id.rvHome, 0)
        EspressoActions.performMenuItemClick(R.string.home_doc_item_share, R.id.share)

        //Assert
        EspressoActions.checkTextOnAlertDialog(R.string.home_alert_error_generate_pdf)
    }

    @Test
    fun delete_doc() {
        //Arrange
        localDataSource = spyk()
        coEvery { localDataSource.getSavedDocs() } returns MutableLiveData(
            AndroidTestUtil.getRealDocs(
                context
            )
        )
        repository = RepositoryImpl(remoteDataSource, localDataSource, context)
        homeViewModel = AndroidTestUtil.getHomeViewModel(context, repository)
        coEvery { localDataSource.getSavedDocs() } returns MutableLiveData(
            AndroidTestUtil.getRealDocs(
                context
            )
        )
        coEvery { localDataSource.deleteDocOnDevice(any()) } returns mockk()

        activityScenario = launchMyMainActivity().onActivity {
            navController = it.findNavController(R.id.nav_host_fragment)
        }

        //Act
        EspressoActions.performLongClickOnRecyclerViewItem(R.id.rvHome, 0)
        EspressoActions.performMenuItemClick(R.string.common_delete_label, R.id.delete)
        EspressoActions.performClickOnText(R.string.alert_dialog_yes_button)

        //Assert
        coVerify { localDataSource.deleteDocOnDevice(any()) }
    }

    @Test
    fun throw_exception_when_delete_doc() {
        //Arrange
        coEvery { localDataSource.deleteDocOnDevice(any()) } throws (RuntimeException(
            Exception(
                context.getString(R.string.home_toast_delete_error)
            )
        ))
        activityScenario = launchMyMainActivity().onActivity {
            navController = it.findNavController(R.id.nav_host_fragment)
        }

        //Act
        EspressoActions.performLongClickOnRecyclerViewItem(R.id.rvHome, 0)
        EspressoActions.performMenuItemClick(R.string.common_delete_label, R.id.delete)
        EspressoActions.performClickOnText(R.string.alert_dialog_yes_button)

        //Assert
        EspressoActions.checkTextOnAlertDialog(R.string.home_toast_delete_error)
    }

    @Test
    fun pick_single_img_from_gallery_and_navigate() {
        //Arrange
        val intent = Intent()
        intent.data = FileUtil.getStubFile(context).toUri()
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, intent)
        intending(hasAction(ACTION_OPEN_DOCUMENT)).respondWith(result)
        activityScenario = launchMyMainActivity().onActivity {
            navController = it.findNavController(R.id.nav_host_fragment)
        }

        //Act
        EspressoActions.performClickOnView(R.id.addButton)
        EspressoActions.performClickOnView(R.id.galleryTv)

        //Assert
        assertThat(navController.currentDestination?.id, Matchers.`is`(R.id.cropFragment))
    }

    @Test
    fun pick_many_img_from_gallery_and_navigate() {
        //Arrange
        val intent = Intent()
        val clipDescription = ClipDescription("Dummy", arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN))
        val uri = FileUtil.getStubFile(context).toUri()
        val clipItem = ClipData.Item(uri)
        intent.clipData = ClipData(clipDescription, clipItem)
        intent.clipData!!.addItem(clipItem)
        intent.clipData!!.addItem(clipItem)
        intent.clipData!!.addItem(clipItem)
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, intent)
        intending(hasAction(ACTION_OPEN_DOCUMENT)).respondWith(result)
        activityScenario = launchMyMainActivity().onActivity {
            navController = it.findNavController(R.id.nav_host_fragment)
        }

        //Act
        EspressoActions.performClickOnView(R.id.addButton)
        EspressoActions.performClickOnView(R.id.galleryTv)

        //Assert
        assertThat(navController.currentDestination?.id, Matchers.`is`(R.id.cropFragment))
    }

    @Test
    fun throw_exception_when_copy_and_navigate_to_crop_fragment() {
        //Arrange
        val intent = Intent()
        intent.data = Uri.EMPTY
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, intent)
        intending(hasAction(ACTION_OPEN_DOCUMENT)).respondWith(result)
        activityScenario = launchMyMainActivity().onActivity {
            navController = it.findNavController(R.id.nav_host_fragment)
        }

        //Act
        EspressoActions.performClickOnView(R.id.addButton)
        EspressoActions.performClickOnView(R.id.galleryTv)

        //Assert
        EspressoActions.checkTextOnAlertDialog(R.string.home_alert_error_copy_image_from_gallery)
    }

    @Test
    fun move_user_to_login_page() {
        //Arrange
        remoteDataSource = mockk()
        repository = RepositoryImpl(remoteDataSource, localDataSource, context)
        homeViewModel = AndroidTestUtil.getHomeViewModel(context, repository)
        coEvery { remoteDataSource.getUser() } throws (RuntimeException(Exception()))

        //Act
        activityScenario = launchMyMainActivity().onActivity {
            navController = it.findNavController(R.id.nav_host_fragment)
        }

        //Assert
        assertThat(navController.currentDestination?.id, Matchers.`is`(R.id.loginFragment))
    }

    @Test
    fun show_docs_on_recycler_view() {

        //Act
        activityScenario = launchMyMainActivity().onActivity {
            navController = it.findNavController(R.id.nav_host_fragment)
        }

        //Assert
        EspressoActions.checkSizeOnRecyclerView(R.id.rvHome, getRealDocs(context).size)
    }

    @Test
    fun tap_doc_navigate_to_edit_fragment() {
        //Arrange
        activityScenario = launchMyMainActivity().onActivity {
            navController = it.findNavController(R.id.nav_host_fragment)
        }

        //Act
        EspressoActions.performClickOnRecyclerViewItem(R.id.rvHome, 0)

        //Assert
        assertThat(navController.currentDestination?.id, Matchers.`is`(R.id.editFragment))
    }

}
//val receivedIntent: Intent = Iterables.getOnlyElement(Intents.getIntents())
//        intended(
//            allOf(
//                hasAction(ACTION_OPEN_DOCUMENT),
//                hasCategories(hasItem(equalTo(Intent.CATEGORY_OPENABLE))),
//                hasExtra(Intent.EXTRA_ALLOW_MULTIPLE, true),
//                hasType("image/*")
//                )
//        )
//assertThat(receivedIntent).hasAction(Intent.ACTION_VIEW)
//assertThat(intent).categories().containsExactly(Intent.CATEGORY_BROWSABLE)
//
//
//intended(
//allOf(
//hasExtras(
//allOf(
////hasEntry(equalTo(Intent.EXTRA_INTENT), hasAction(ACTION_OPEN_DOCUMENT)),
//hasEntry(equalTo(Intent.EXTRA_INTENT), hasType("image/*")),
//)
//),
//hasCategories(hasItem(equalTo(Intent.CATEGORY_OPENABLE))),
//hasAction(equalTo(ACTION_OPEN_DOCUMENT))
//)
//)
//)