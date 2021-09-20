package com.demo.doccloud.ui.home

import android.app.Activity
import android.app.Instrumentation
import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_OPEN_DOCUMENT
import android.net.Uri
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openContextualActionModeOverflowMenu
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.BundleMatchers.hasEntry
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.demo.doccloud.*
import com.demo.doccloud.domain.usecases.impl.*
import com.demo.doccloud.ui.MainActivity
import com.demo.doccloud.utils.AppConstants
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.Matchers
import org.hamcrest.Matchers.equalTo
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject


@HiltAndroidTest
@MediumTest
@ExperimentalCoroutinesApi
class HomeFragmentTest {

    private lateinit var activityScenario: ActivityScenario<MainActivity>
    private lateinit var navController: NavController
    private lateinit var dataBindingIdlingResource: DataBindingIdlingResource

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @BindValue
    lateinit var homeViewModel: HomeViewModel

    @Inject
    lateinit var repository: FakeRepository
    private lateinit var mIdlingResource: IdlingResource
    private lateinit var context: Context

    @Before
    fun setup() {
        hiltRule.inject()
        context = InstrumentationRegistry.getInstrumentation().targetContext
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
        Intents.init()
        activityScenario = launchMyMainActivity().onActivity {
            navController = it.findNavController(R.id.nav_host_fragment)
        }
        mIdlingResource = EspressoIdlingResource.countingIdlingResource
        IdlingRegistry.getInstance().register(mIdlingResource)

        dataBindingIdlingResource = DataBindingIdlingResource()
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
        dataBindingIdlingResource.monitorActivity(activityScenario)


        repository.setHasDelay(true)
        GlobalVariablesTest.hasDelay = true
    }

    @After
    fun teardown() {
        IdlingRegistry.getInstance().unregister(mIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
        repository.setHasDelay(false)
        GlobalVariablesTest.hasDelay = false
        GlobalVariablesTest.shouldThrowException = false
        activityScenario.close()
        Intents.release()
    }

    @Test
    fun do_logout_successfully() {
        openContextualActionModeOverflowMenu()
        EspressoActions.performMenuItemClick(R.string.home_menu_logout, R.id.logout)
        assertThat(navController.currentDestination?.id, Matchers.`is`(R.id.loginFragment))
    }

    @Test
    fun share_doc_successfully() {
        val intent = Intent()
        intent.putExtra(Intent.EXTRA_STREAM, Uri.EMPTY)
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, intent)

        intending(hasAction(Intent.ACTION_SEND)).respondWith(result);
        EspressoActions.performLongClickRecyclerViewItem(R.id.rvHome, 0)
        EspressoActions.performMenuItemClick(R.string.home_doc_item_share, R.id.share)

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
        GlobalVariablesTest.shouldThrowException = true
        EspressoActions.performLongClickRecyclerViewItem(R.id.rvHome, 0)
        EspressoActions.performMenuItemClick(R.string.home_doc_item_share, R.id.share)
        EspressoActions.checkTextOnAlertDialog(R.string.home_alert_error_generate_pdf)
    }

    @Test
    fun delete_doc() {
        activityScenario.onActivity {
            it.runOnUiThread {
                val value = homeViewModel.docs.getOrAwaitValue()
                assertThat(value.size, Matchers.`is`(2))
            }
        }
        EspressoActions.performLongClickRecyclerViewItem(R.id.rvHome, 0)
        EspressoActions.performMenuItemClick(R.string.common_delete_label, R.id.delete)
        EspressoActions.performClickOnText(R.string.alert_dialog_yes_button)
        activityScenario.onActivity {
            it.runOnUiThread {
                val value = homeViewModel.docs.getOrAwaitValue()
                assertThat(value.size, Matchers.`is`(1))
            }
        }
    }

    @Test
    fun throw_exception_when_delete_doc() {
        repository.setShouldThrowExceptionWhenDeleteLocalDoc(true)
        EspressoActions.performLongClickRecyclerViewItem(R.id.rvHome, 0)
        EspressoActions.performMenuItemClick(R.string.common_delete_label, R.id.delete)
        EspressoActions.performClickOnText(R.string.alert_dialog_yes_button)
        EspressoActions.checkTextOnAlertDialog(R.string.home_toast_delete_error)
    }

    @Test
    fun pick_single_img_from_gallery_and_navigate() {
        val intent = Intent()
        intent.data = Uri.EMPTY
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, intent)
        intending(hasAction(ACTION_OPEN_DOCUMENT)).respondWith(result)
        EspressoActions.performClickOnView(R.id.addButton)
        EspressoActions.performClickOnView(R.id.galleryTv)
        assertThat(navController.currentDestination?.id, Matchers.`is`(R.id.cropFragment))
    }

    @Test
    fun pick_many_img_from_gallery_and_navigate() {
        val intent = Intent()
        val clipDescription = ClipDescription("Dummy", arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN))
        val uri = Uri.EMPTY
        val clipItem = ClipData.Item(uri)
        intent.clipData = ClipData(clipDescription, clipItem)
        intent.clipData!!.addItem(clipItem)
        intent.clipData!!.addItem(clipItem)
        intent.clipData!!.addItem(clipItem)


        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, intent)
        intending(hasAction(ACTION_OPEN_DOCUMENT)).respondWith(result)
        EspressoActions.performClickOnView(R.id.addButton)
        EspressoActions.performClickOnView(R.id.galleryTv)
        assertThat(navController.currentDestination?.id, Matchers.`is`(R.id.cropFragment))
    }

    @Test
    fun throw_exception_when_copy_and_navigate_to_crop_fragment(){
        GlobalVariablesTest.shouldThrowException = true
        val intent = Intent()
        intent.data = Uri.EMPTY
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, intent)
        intending(hasAction(ACTION_OPEN_DOCUMENT)).respondWith(result)
        EspressoActions.performClickOnView(R.id.addButton)
        EspressoActions.performClickOnView(R.id.galleryTv)
        EspressoActions.checkTextOnAlertDialog(R.string.home_alert_error_copy_image_from_gallery)
    }

    @Test
    fun move_user_to_login_page(){
        repository.setShouldThrowExceptionWhenGetUser(true)
        homeViewModel.setupInitVariables()
        activityScenario.onActivity {
            it.runOnUiThread{
                homeViewModel.navigationCommands.getOrAwaitValue()
            }
        }
        assertThat(navController.currentDestination?.id, Matchers.`is`(R.id.loginFragment))
        //homeViewModel.setupInitVariables()
        //onIdle()
        //EspressoActions.checkTextOnScreen(R.string.login_doc_cloud_name)
        //assertThat(navController.currentDestination?.id, Matchers.`is`(R.id.loginFragment))
    }

    @Test
    fun show_two_docs_on_recycler_view(){
        onView(withId(R.id.rvHome)).check(matches(EspressoActions.hasItemCountOnRecyclerView(2)))
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