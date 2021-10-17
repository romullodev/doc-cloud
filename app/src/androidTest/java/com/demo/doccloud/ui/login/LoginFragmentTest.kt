package com.demo.doccloud.ui.login


import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onIdle
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
import com.demo.doccloud.domain.usecases.contracts.*
import com.demo.doccloud.domain.usecases.impl.*
import com.demo.doccloud.ui.MainActivity
import com.demo.doccloud.ui.AndroidTestUtil
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
import org.mockito.MockitoAnnotations

@HiltAndroidTest
@MediumTest
@ExperimentalCoroutinesApi
class LoginFragmentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var context: Context
    private lateinit var activityScenario: ActivityScenario<MainActivity>
    private lateinit var navController: NavController

    @BindValue
    lateinit var repository: RepositoryImpl

    @BindValue
    lateinit var loginViewModel: LoginViewModel
    private lateinit var mIdlingResource: IdlingResource
    private val localDataSource: LocalDataSource = mockk()
    private val remoteDataSource: RemoteDataSource = mockk()

    @Before
    fun setup() {
        hiltRule.inject()
        MockitoAnnotations.initMocks(this);
        context = InstrumentationRegistry.getInstrumentation().targetContext

        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()

        // Initialize WorkManager for instrumentation tests.
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
        repository = RepositoryImpl(remoteDataSource, localDataSource, context)
        coEvery { repository.docs } returns MutableLiveData()

        val scheduleToSyncDataUseCase = ScheduleToSyncDataImpl(context)
        val saveCustomIdSyncStrategy = SaveCustomIdSyncStrategyImpl(repository)
        val sendCustomIdAndForceUpdate = SendCustomIdAndForceUpdateImpl(repository)
        val doLoginWithGoogleUseCase =
            DoLoginWithGoogleImpl(saveCustomIdSyncStrategy, sendCustomIdAndForceUpdate, repository)

        val doLoginByEmailUseCase =
            DoLoginByEmailImpl(saveCustomIdSyncStrategy, sendCustomIdAndForceUpdate, repository)
        loginViewModel = LoginViewModel(
            scheduleToSyncDataUseCase,
            doLoginWithGoogleUseCase,
            doLoginByEmailUseCase
        )

        activityScenario = launchMyMainActivity().onActivity {
            navController = it.findNavController(R.id.nav_host_fragment)
        }

        mIdlingResource = EspressoIdlingResource.countingIdlingResource
        IdlingRegistry.getInstance().register(mIdlingResource)
    }

    @After
    fun teardown() {
        IdlingRegistry.getInstance().unregister(mIdlingResource)
        activityScenario.close()
    }

    @Test
    fun do_login_with_google_successfully() {
        //Arrange
        coEvery { remoteDataSource.doLoginWithGoogle(any()) } coAnswers {
            wrapEspressoIdlingResource {
                return@wrapEspressoIdlingResource AndroidTestUtil.getUser()
            }
        }
        coEvery { localDataSource.saveLong(any(), any()) } returns mockk()//coAnswers { wrapEspressoIdlingResource {} }
        coEvery { remoteDataSource.sendCustomIdForceUpdate(any()) } //coAnswers { wrapEspressoIdlingResource {} }
        coEvery { remoteDataSource.sendCustomIdForceUpdate(any()) } returns mockk()

        //Act
        activityScenario.onActivity {
            it.runOnUiThread {
                loginViewModel.doLoginWithGoogle(null)
            }
        }
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
        coEvery { remoteDataSource.doLoginWithGoogle(any()) } throws (RuntimeException(
            context.getString(
                R.string.login_error_api_google
            )
        ))

        //Act
        activityScenario.onActivity {
            it.runOnUiThread {
                loginViewModel.doLoginWithGoogle(null)
            }
        }

        //Assert
        EspressoActions.checkTextOnAlertDialog(R.string.login_error_api_google)
    }

    @Test
    fun throw_user_with_no_id_exception() {
        // Arrange
        coEvery { remoteDataSource.doLoginWithGoogle(any()) } coAnswers {
            wrapEspressoIdlingResource {
                throw (RuntimeException(context.getString(R.string.login_user_with_no_id)))
            }
        }

        //Act
        activityScenario.onActivity {
            it.runOnUiThread {
                loginViewModel.doLoginWithGoogle(null)
            }
        }
        EspressoActions.checkTextOnAlertDialog(R.string.login_user_with_no_id)
    }

    @Test
    fun throw_unknown_exception() {
        // Arrange
        coEvery { remoteDataSource.doLoginWithGoogle(any()) } coAnswers {
            wrapEspressoIdlingResource {
                throw (RuntimeException(context.getString(R.string.common_unknown_error)))
            }
        }

        //Act
        activityScenario.onActivity {
            it.runOnUiThread {
                loginViewModel.doLoginWithGoogle(null)
            }
        }

        //Assert
        EspressoActions.checkTextOnAlertDialog(R.string.common_unknown_error)
    }

    @Test
    fun verify_loading_state() {
        // Arrange
        coEvery { remoteDataSource.doLoginWithGoogle(any()) } coAnswers {
            wrapEspressoIdlingResource {
                delay(3000)
                return@wrapEspressoIdlingResource AndroidTestUtil.getUser()
            }
        }
        IdlingRegistry.getInstance().unregister(mIdlingResource)

        //Act
        activityScenario.onActivity {
            it.runOnUiThread {
                loginViewModel.doLoginWithGoogle(null)
            }
        }

        //Assert
        EspressoActions.checkTextOnScreen(R.string.loading_dialog_message_login)
    }

}