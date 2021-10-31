package com.demo.doccloud.ui.licences

import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.findNavController
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.demo.doccloud.*
import com.demo.doccloud.data.repository.RepositoryImpl
import com.demo.doccloud.domain.entities.Photo
import com.demo.doccloud.domain.usecases.contracts.*
import com.demo.doccloud.domain.usecases.impl.GetAppLicencesFromServerImpl
import com.demo.doccloud.idling.EspressoIdlingResource
import com.demo.doccloud.ui.MainActivity
import com.demo.doccloud.ui.home.HomeFragmentDirections
import com.demo.doccloud.ui.home.HomeViewModel
import com.demo.doccloud.utils.BackToRoot
import com.demo.doccloud.utils.Global
import com.demo.doccloud.utils.ListPhotoArg
import com.demo.doccloud.utils.RootDestination
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.lang.RuntimeException
import javax.inject.Inject

@HiltAndroidTest
@MediumTest
@ExperimentalCoroutinesApi
class LicensesFragmentTest{
    private lateinit var activityScenario: ActivityScenario<HiltTestActivity>
    private lateinit var navController: NavController

    private lateinit var mIdlingResource: IdlingResource
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var repository: RepositoryImpl
    @BindValue
    lateinit var licensesViewModel: LicensesViewModel

    @Before
    fun setup(){
        hiltRule.inject()
        licensesViewModel = LicensesViewModel(GetAppLicencesFromServerImpl(repository))
        mIdlingResource = EspressoIdlingResource.countingIdlingResource
        IdlingRegistry.getInstance().register(mIdlingResource)
    }

    @After
    fun teardown(){
        IdlingRegistry.getInstance().unregister(mIdlingResource)
        activityScenario.close()
        Global.licensesCache = listOf()
    }

    private fun launchFragment() {
        navController = TestNavHostController(InstrumentationRegistry.getInstrumentation().targetContext)
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            navController.setGraph(R.navigation.nav_graph)
        }
        activityScenario = launchFragmentInHiltContainerNavHostVersion<LicensesFragment>(
            navController = navController
        )
    }

    @Test
    fun showAllLicenses(){
        //Act
        launchFragment()

        //Assert
        onIdle()
        EspressoActions.checkSizeOnRecyclerView(R.id.recyclerView, 6)
    }

    @Test
    fun backToHomeAfterFailure(){
        //Arrange
        val getLicensesMock : GetAppLicencesFromServer = mockk()
        licensesViewModel = LicensesViewModel(getLicensesMock)
        coEvery { getLicensesMock() } throws RuntimeException()

        //Act
        launchFragment()

        //Arrange
        EspressoActions.checkTextOnAlertDialog(R.string.licenses_screen_error_on_load_licenses)
    }

}