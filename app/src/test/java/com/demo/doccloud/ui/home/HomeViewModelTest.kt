package com.demo.doccloud.ui.home

import androidx.test.core.app.ApplicationProvider
import com.demo.doccloud.MainCoroutineRule
import com.demo.doccloud.data.repository.FakeRepository
import com.demo.doccloud.getOrAwaitValue
import com.demo.doccloud.ui.login.LoginViewModel
import com.google.common.truth.Truth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@ExperimentalCoroutinesApi
class HomeViewModelTest{


    private lateinit var repository: FakeRepository
    private lateinit var homeViewModel: HomeViewModel

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setup() {
        repository = FakeRepository(ApplicationProvider.getApplicationContext())
        homeViewModel = HomeViewModel(repository)
    }

    @Test
    fun `do logout with success`() = mainCoroutineRule.runBlockingTest{
        homeViewModel.doLogout()
        val value = homeViewModel.navigationCommands.getOrAwaitValue()
        Truth.assertThat((value.getContentIfNotHandled() as HomeViewModel.NavigationCommand.To).directions)
            .isEqualTo(
                HomeFragmentDirections.actionHomeFragmentToLoginFragment()
            )
    }

}