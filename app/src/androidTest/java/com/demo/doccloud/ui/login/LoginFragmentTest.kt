package com.demo.doccloud.ui.login


import android.content.Context
import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.intent.Intents
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
import com.demo.doccloud.domain.usecases.contracts.*
import com.demo.doccloud.domain.usecases.impl.*
import com.demo.doccloud.idling.EspressoIdlingResource
import com.demo.doccloud.idling.wrapEspressoIdlingResource
import com.demo.doccloud.ui.AndroidTestUtil
import com.demo.doccloud.ui.MainActivity
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
@MediumTest
@ExperimentalCoroutinesApi
class LoginFragmentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var context: Context
    private lateinit var activityScenario: ActivityScenario<MainActivity>
    private lateinit var navController: NavController
    private lateinit var mIdlingResource: IdlingResource

    @BindValue
    lateinit var repository: RepositoryImpl

    private lateinit var localDataSource: AppLocalServices
    private val mockRemoteDataSource: RemoteDataSource = mockk()

    private fun launchScreen() {
        activityScenario = launchMyMainActivity().onActivity {
            navController = it.findNavController(R.id.nav_host_fragment)
        }
    }

    @Before
    fun setup() {
        hiltRule.inject()
        context = InstrumentationRegistry.getInstrumentation().targetContext
        localDataSource = AndroidTestUtil.getLocalDataSource(context)
        repository = RepositoryImpl(mockRemoteDataSource, localDataSource, context)

        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()

        // Initialize WorkManager for instrumentation tests.
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)

        mIdlingResource = EspressoIdlingResource.countingIdlingResource
        IdlingRegistry.getInstance().register(mIdlingResource)
        Intents.init()
    }

    @After
    fun teardown() {
        IdlingRegistry.getInstance().unregister(mIdlingResource)
        activityScenario.close()
        Intents.release()
    }

    @Test
    fun do_login_with_google_successfully() {
        //Arrange
        coEvery { mockRemoteDataSource.doLoginWithGoogle(any()) } coAnswers {
            wrapEspressoIdlingResource {
                return@wrapEspressoIdlingResource AndroidTestUtil.getUser()
            }
        }
        coEvery { mockRemoteDataSource.sendCustomIdForceUpdate(any()) }
        coEvery { mockRemoteDataSource.sendCustomIdForceUpdate(any()) } returns mockk()
        launchScreen()

        //Act
        EspressoActions.performClickOnView(R.id.buttonLoginSignInGoogle)
        onIdle()

        //Assert
        ViewMatchers.assertThat(
            navController.currentDestination?.id,
            Matchers.`is`(R.id.homeFragment)
        )
    }

    @Test
    fun throw_google_api_exception() {
        //Arrange
        coEvery { mockRemoteDataSource.doLoginWithGoogle(any()) } throws (RuntimeException(
            context.getString(
                R.string.login_error_api_google
            )
        ))
        launchScreen()

        //Act
        EspressoActions.performClickOnView(R.id.buttonLoginSignInGoogle)

        //Assert
        EspressoActions.checkTextOnAlertDialog(R.string.login_error_api_google)
    }


    @Test
    fun throw_user_with_no_id_exception() {
        // Arrange
        coEvery { mockRemoteDataSource.doLoginWithGoogle(any()) } coAnswers {
            wrapEspressoIdlingResource {
                throw (RuntimeException(context.getString(R.string.login_user_with_no_id)))
            }
        }
        launchScreen()

        //Act
        EspressoActions.performClickOnView(R.id.buttonLoginSignInGoogle)

        //Assert
        EspressoActions.checkTextOnAlertDialog(R.string.login_user_with_no_id)
    }

    @Test
    fun throw_unknown_exception() {
        // Arrange
        coEvery { mockRemoteDataSource.doLoginWithGoogle(any()) } coAnswers {
            wrapEspressoIdlingResource {
                throw (RuntimeException(context.getString(R.string.common_unknown_error)))
            }
        }
        launchScreen()

        //Act
        EspressoActions.performClickOnView(R.id.buttonLoginSignInGoogle)

        //Assert
        EspressoActions.checkTextOnAlertDialog(R.string.common_unknown_error)
    }

    @Test
    fun verify_loading_state() {
        // Arrange
        coEvery { mockRemoteDataSource.doLoginWithGoogle(any()) } coAnswers {
            wrapEspressoIdlingResource {
                delay(3000)
                return@wrapEspressoIdlingResource AndroidTestUtil.getUser()
            }
        }
        IdlingRegistry.getInstance().unregister(mIdlingResource)
        launchScreen()

        //Act
        EspressoActions.performClickOnView(R.id.buttonLoginSignInGoogle)

        //Assert
        EspressoActions.checkTextOnScreen(R.string.loading_dialog_message_login)
    }

}