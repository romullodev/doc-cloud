package com.demo.doccloud.ui.login


import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.demo.doccloud.*
import com.demo.doccloud.FakeRepository
import com.demo.doccloud.domain.usecases.impl.DoLoginWithGoogleImpl
import com.demo.doccloud.domain.usecases.impl.SaveCustomIdSyncStrategyImpl
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import javax.inject.Inject

@HiltAndroidTest
@MediumTest
@ExperimentalCoroutinesApi
class LoginFragmentTest {

    private lateinit var activityScenario: ActivityScenario<HiltTestActivity>
    private lateinit var navController: NavController

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var repository: FakeRepository

    @BindValue
    lateinit var loginViewModel: LoginViewModel

    private lateinit var mIdlingResource: IdlingResource

    @Before
    fun setup() {
        hiltRule.inject()

        val fakeScheduleToSyncData = FakeScheduleToSyncDataImpl()
        val saveCustomIdSyncStrategy = SaveCustomIdSyncStrategyImpl(repository)
        val doLoginWithGoogle = DoLoginWithGoogleImpl(saveCustomIdSyncStrategy, repository)
        loginViewModel = LoginViewModel(fakeScheduleToSyncData, doLoginWithGoogle)

        navController = mock(NavController::class.java)
        activityScenario = launchFragmentInHiltContainer<LoginFragment> {
            Navigation.setViewNavController(requireView(), navController)
        }
        mIdlingResource = EspressoIdlingResource.countingIdlingResource
        IdlingRegistry.getInstance().register(mIdlingResource)
        repository.setHasDelay(true)
    }

    @After
    fun teardown() {
        activityScenario.close()
        IdlingRegistry.getInstance().unregister(mIdlingResource)
        repository.setHasDelay(false)
    }

    @Test
    fun do_login_with_google_successfully() {
        activityScenario.onActivity {
            it.runOnUiThread {
                loginViewModel.doLoginWithGoogle(null)
            }
        }
        onIdle()
        verify(navController).popBackStack()
    }

    @Test
    fun throw_google_api_exception() {
        repository.setShouldThrowApiException(true)
        activityScenario.onActivity {
            it.runOnUiThread {
                loginViewModel.doLoginWithGoogle(null)
            }
        }
        EspressoActions.checkTextOnAlertDialog(R.string.login_error_api_google)
    }

    @Test
    fun throw_user_with_no_id_exception() {
        repository.setShouldThrowUserWithNoIdException(true)
        activityScenario.onActivity {
            it.runOnUiThread {
                loginViewModel.doLoginWithGoogle(null)
            }
        }
        EspressoActions.checkTextOnAlertDialog(R.string.login_user_with_no_id)
    }

    @Test
    fun throw_unknown_exception() {
        repository.setShouldThrowUnknownException(true)
        activityScenario.onActivity {
            it.runOnUiThread {
                loginViewModel.doLoginWithGoogle(null)
            }
        }
        EspressoActions.checkTextOnAlertDialog(R.string.common_unknown_error)
    }

    @Test
    fun verify_loading_state() {
        IdlingRegistry.getInstance().unregister(mIdlingResource)
        activityScenario.onActivity {
            it.runOnUiThread {
                loginViewModel.doLoginWithGoogle(null)
            }
        }
        EspressoActions.checkTextOnScreen(R.string.loading_dialog_message_login)
        //assertThat(loadingValue, Matchers.`is`(true))
    }
}