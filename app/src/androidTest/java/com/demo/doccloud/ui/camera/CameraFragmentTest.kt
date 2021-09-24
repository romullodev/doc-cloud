package com.demo.doccloud.ui.camera

import FileUtil.getStubFile
import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers.anyIntent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.demo.doccloud.*
import com.demo.doccloud.domain.entities.Photo
import com.demo.doccloud.domain.usecases.impl.*
import com.demo.doccloud.ui.MainActivity
import com.demo.doccloud.ui.crop.CropViewModel
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
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
@MediumTest
@ExperimentalCoroutinesApi
class CameraFragmentTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var activityScenario: ActivityScenario<MainActivity>
    private lateinit var navController: NavController

    @BindValue
    lateinit var cameraViewModel: CameraViewModel

    @Inject
    lateinit var repository: FakeRepository
    private val mIdlingResource: IdlingResource = EspressoIdlingResource.countingIdlingResource
    private lateinit var context: Context

    @Before
    fun setup() {
        hiltRule.inject()
        context = InstrumentationRegistry.getInstrumentation().targetContext
        cameraViewModel = CameraViewModel()
        activityScenario = launchFromMainActivityToFragment(
            direction = HomeFragmentDirections.actionHomeFragmentToCameraFragment(
                root = BackToRoot(
                    rootDestination = RootDestination.HOME_DESTINATION
                )
            )
        ).onActivity {
            navController = it.findNavController(R.id.nav_host_fragment)
        }
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
    }

    @Test
    fun capture_photos() {
        EspressoActions.performClickOnView(R.id.capture_btn)
        Thread.sleep(2000)
        EspressoActions.performClickOnView(R.id.capture_btn)
        Thread.sleep(2000)
        EspressoActions.performClickOnView(R.id.capture_btn)
        Thread.sleep(2000)
        EspressoActions.checkSizeOnRecyclerView(R.id.rv_thumbnails, 3)
    }
    @Test
    fun capture_photos_and_continue() {
        EspressoActions.checkIsNotVisible(R.id.finish_photos)
        EspressoActions.performClickOnView(R.id.capture_btn)
        Thread.sleep(2000)
        EspressoActions.checkIsVisible(R.id.finish_photos)
        EspressoActions.performClickOnView(R.id.finish_photos)
        ViewMatchers.assertThat(
            navController.currentDestination?.id,
            Matchers.`is`(R.id.cropFragment)
        )
    }
    @Test
    fun check_continue_button_visibility() {
        EspressoActions.checkIsNotVisible(R.id.finish_photos)
        EspressoActions.performClickOnView(R.id.capture_btn)
        Thread.sleep(2000)
        EspressoActions.checkIsVisible(R.id.finish_photos)
    }


}