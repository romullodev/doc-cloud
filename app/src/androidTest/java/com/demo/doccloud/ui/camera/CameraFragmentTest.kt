package com.demo.doccloud.ui.camera

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
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
import com.demo.doccloud.ui.MainActivity
import com.demo.doccloud.ui.AndroidTestUtil
import com.demo.doccloud.ui.home.HomeFragmentDirections
import com.demo.doccloud.utils.BackToRoot
import com.demo.doccloud.utils.RootDestination
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
@MediumTest
@ExperimentalCoroutinesApi
class CameraFragmentTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var activityScenario: ActivityScenario<MainActivity>
    private lateinit var navController: NavController

    @BindValue
    val cameraViewModel: CameraViewModel = CameraViewModel()

    @BindValue
    lateinit var repository: RepositoryImpl
    private val mIdlingResource: IdlingResource = EspressoIdlingResource.countingIdlingResource
    private lateinit var context: Context

    @Before
    fun setup() {
        hiltRule.inject()
        context = InstrumentationRegistry.getInstrumentation().targetContext

        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()

        // Initialize WorkManager for instrumentation tests.
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)

        val localDataSource = mockk<LocalDataSource>()
        val remoteDataSource = mockk<RemoteDataSource>()
        repository = RepositoryImpl(remoteDataSource, localDataSource, context)

        coEvery { remoteDataSource.getUser() } returns AndroidTestUtil.getUser()
        coEvery { repository.docs } returns MutableLiveData()

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
    }

    @After
    fun teardown() {
        IdlingRegistry.getInstance().unregister(mIdlingResource)
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