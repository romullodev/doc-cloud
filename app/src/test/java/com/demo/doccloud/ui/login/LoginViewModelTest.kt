package com.demo.doccloud.ui.login

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.demo.doccloud.MainCoroutineRule
import com.demo.doccloud.R
import com.demo.doccloud.data.repository.FakeRepository
import com.demo.doccloud.domain.usecases.impl.DoLoginWithGoogleImpl
import com.demo.doccloud.domain.usecases.impl.SaveCustomIdSyncStrategyImpl
import com.demo.doccloud.getOrAwaitValue
import com.demo.doccloud.fakes.FakeScheduleToSyncDataImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import com.google.common.truth.Truth.assertThat

@RunWith(RobolectricTestRunner::class)
@ExperimentalCoroutinesApi
class LoginViewModelTest{

    private lateinit var repository: FakeRepository
    private lateinit var loginViewModel: LoginViewModel

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setup() {
        repository = FakeRepository(ApplicationProvider.getApplicationContext())
        val fakeScheduleToSyncData = FakeScheduleToSyncDataImpl()
        val saveCustomIdSyncStrategy = SaveCustomIdSyncStrategyImpl(repository)
        val doLoginWithGoogle = DoLoginWithGoogleImpl(saveCustomIdSyncStrategy, repository)
        loginViewModel = LoginViewModel(fakeScheduleToSyncData, doLoginWithGoogle)
    }

    @Test
    fun `do login with success`() = mainCoroutineRule.runBlockingTest{
        loginViewModel.doLoginWithGoogle(null)
        val value = loginViewModel.loginState.getOrAwaitValue()
        assertThat(value.getContentIfNotHandled()).isInstanceOf(LoginViewModel.LoginState.Authenticated::class.java)
    }

    @Test
    fun `do login with empty username field`() = mainCoroutineRule.runBlockingTest {
        loginViewModel.password = "any"
        loginViewModel.doLogin()
        val value = loginViewModel.loginState.getOrAwaitValue()
        assertThat(value.getContentIfNotHandled()).isInstanceOf(LoginViewModel.LoginState.InvalidCredentials::class.java)
    }

    @Test
    fun `do login and get no internet dialog msg`() = mainCoroutineRule.runBlockingTest{
        repository.setShouldThrowNetworkingException(true)
        loginViewModel.doLoginWithGoogle(null)
        val value = loginViewModel.loginState.getOrAwaitValue()
        assertThat((value.getContentIfNotHandled() as LoginViewModel.LoginState.LoginAlertDialog).msg)
            .isEqualTo(ApplicationProvider.getApplicationContext<Context>().getString(R.string.common_no_internet))
    }

    @Test
    fun `do login and get an error from google api`() = mainCoroutineRule.runBlockingTest{
        repository.setShouldThrowApiException(true)
        loginViewModel.doLoginWithGoogle(null)
        val value = loginViewModel.loginState.getOrAwaitValue()
        assertThat((value.getContentIfNotHandled() as LoginViewModel.LoginState.LoginAlertDialog).msg)
            .isEqualTo(ApplicationProvider.getApplicationContext<Context>().getString(R.string.login_error_api_google))
    }

    @Test
    fun `do login and get user with no id Exception`() = mainCoroutineRule.runBlockingTest{
        repository.setShouldThrowUserWithNoIdException(true)
        loginViewModel.doLoginWithGoogle(null)
        val value = loginViewModel.loginState.getOrAwaitValue()
        assertThat((value.getContentIfNotHandled() as LoginViewModel.LoginState.LoginAlertDialog).msg)
            .isEqualTo(ApplicationProvider.getApplicationContext<Context>().getString(R.string.login_user_with_no_id))
    }

    @Test
    fun `do login and get unknown Exception`() = mainCoroutineRule.runBlockingTest{
        repository.setShouldThrowUnknownException(true)
        loginViewModel.doLoginWithGoogle(null)
        val value = loginViewModel.loginState.getOrAwaitValue()
        assertThat((value.getContentIfNotHandled() as LoginViewModel.LoginState.LoginAlertDialog).msg)
            .isEqualTo(ApplicationProvider.getApplicationContext<Context>().getString(R.string.common_unknown_error))
    }


}