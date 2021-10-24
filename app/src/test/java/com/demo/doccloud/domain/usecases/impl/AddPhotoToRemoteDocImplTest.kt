package com.demo.doccloud.domain.usecases.impl

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.demo.doccloud.data.datasource.local.AppLocalServices
import com.demo.doccloud.data.datasource.local.room.entities.DatabaseDoc
import com.demo.doccloud.data.datasource.remote.FirebaseServices
import com.demo.doccloud.data.repository.RepositoryImpl
import com.demo.doccloud.domain.entities.Photo
import com.google.gson.Gson
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [30])
@RunWith(RobolectricTestRunner::class)
class AddPhotoToRemoteDocImplTest{

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `check all params arrived properly`() = runBlocking {
        //Arrange
        val remoteDatasourceMock = mockk<FirebaseServices>()
        val localDatasourceMock = mockk<AppLocalServices>()
        val repository = RepositoryImpl(
            remoteDatasource = remoteDatasourceMock,
            localDatasource = localDatasourceMock,
            context
        )
        val photosSlot = slot<List<Photo>>()
        val newJsonPagesSlot = slot<String>()
        coEvery { remoteDatasourceMock.addPhotosDoc(any(), capture(photosSlot), capture(newJsonPagesSlot))} returns mockk()
        val photos = arrayListOf(Photo(id = 1L, path = ""), Photo(id = 2L, path = ""))
        val id = -1L
        val newJsonPhotos = Gson().toJson(photos)
        val addPhotoToRemoteDocImpl = AddPhotoToRemoteDocImpl(repository)

        //Act
        addPhotoToRemoteDocImpl(id, photos, newJsonPhotos)

        //Assert
        coVerify {
            remoteDatasourceMock.addPhotosDoc(eq(id), eq(photos), eq(newJsonPhotos))
        }
    }
}