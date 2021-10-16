package com.demo.doccloud.ui.camera

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.demo.doccloud.FakeRepository
import com.demo.doccloud.GlobalVariablesTest
import com.demo.doccloud.MainCoroutineRule
import com.demo.doccloud.domain.entities.Photo
import com.demo.doccloud.getOrAwaitValue
import com.google.common.truth.Truth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@ExperimentalCoroutinesApi
class CameraViewModelTest {

    private lateinit var repository: FakeRepository
    private lateinit var cameraViewModel: CameraViewModel
    private lateinit var context: Context

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        repository = FakeRepository(context)
        cameraViewModel = CameraViewModel()
    }

    @After
    fun teardown() {
        GlobalVariablesTest.clearFlags()
        repository.clearFlags()
    }

    @Test
    fun `add photos`() {
        cameraViewModel.addItem(Photo(id = 1L, path = ""))
        cameraViewModel.addItem(Photo(id = 2L, path = ""))
        cameraViewModel.addItem(Photo(id = 3L, path = ""))

        val value = cameraViewModel.listThumbnail.getOrAwaitValue()
        Truth.assertThat(value.size).isEqualTo(3)
    }

    @Test
    fun `delete all photos`() {
        cameraViewModel.addItem(Photo(id = 1L, path = ""))
        cameraViewModel.addItem(Photo(id = 2L, path = ""))
        cameraViewModel.addItem(Photo(id = 3L, path = ""))

        val value1 = cameraViewModel.listThumbnail.getOrAwaitValue()
        Truth.assertThat(value1.size).isEqualTo(3)
        cameraViewModel.deleteAllItem()
        val value2 = cameraViewModel.listThumbnail.getOrAwaitValue()
        Truth.assertThat(value2.size).isEqualTo(0)
    }
}