package com.demo.doccloud.data.repository

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import androidx.lifecycle.LiveData
import androidx.work.*
import com.demo.doccloud.R
import com.demo.doccloud.data.datasource.local.LocalDataSource
import com.demo.doccloud.data.datasource.remote.RemoteDataSource
import com.demo.doccloud.di.IoDispatcher
import com.demo.doccloud.domain.entities.*
import com.demo.doccloud.utils.AppConstants
import com.demo.doccloud.utils.Global
import com.demo.doccloud.utils.Result
import com.demo.doccloud.workers.*
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RepositoryImpl @Inject constructor(
    private val remoteDatasource: RemoteDataSource,
    private val localDatasource: LocalDataSource,
    @ApplicationContext private val context: Context,
) : Repository {
    override val docs: LiveData<List<Doc>> get() = localDatasource.getSavedDocs()

    override suspend fun doLoginWithGoogle(data: Intent?) =
        remoteDatasource.doLoginWithGoogle(data)

    override suspend fun doLoginByEmail(email: String, password: String) =
        remoteDatasource.doLoginByEmail(email, password)


    override suspend fun registerUser(params: SignUpParams) = remoteDatasource.registerUser(params)

    override suspend fun recoverPassword(email: String) = remoteDatasource.recoverPassword(email)

    override suspend fun getUser() = remoteDatasource.getUser()

    override suspend fun doLogout() {
        localDatasource.clearAllData()
        remoteDatasource.doLogout()
    }

    override suspend fun saveDoc(doc: Doc): Long {
        return localDatasource.saveDocOnDevice(doc)
    }

    override suspend fun deleteDoc(doc: Doc) {
        localDatasource.deleteDocOnDevice(doc)
    }

    override suspend fun getDoc(id: Long) = localDatasource.getDoc(id)


    override suspend fun updateDocName(localId: Long, name: String) {
        localDatasource.updateDocName(localId, name)
    }

    override suspend fun deleteDocPhoto(localId: Long, photo: Photo) {
        localDatasource.deleteDocPhoto(
            localId = localId,
            photo = photo
        )
    }

    override suspend fun scheduleToSyncData() {
        //setup constraint to workManager (only send if network is available)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        //setup the request work to send
        val syncData =
            OneTimeWorkRequestBuilder<SyncDataWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

        //schedule the work to be done
        WorkManager.getInstance(context).enqueue(syncData)
    }

    override suspend fun addPhotos(localId: Long, photos: List<Photo>) {
        localDatasource.addPhotosToDoc(localId = localId, photos = photos)
        //setupAddPhotoDocSchedule(localId = localId, photosId = photos.map { it.id })
    }

    override suspend fun saveLong(key: String, value: Long) {
        localDatasource.saveLong(key, value)
    }

    override suspend fun getLong(key: String, defaultValue: Long) =
        localDatasource.getLong(key, defaultValue)

    override suspend fun updateLocalDoc(doc: Doc) {
        localDatasource.updateDoc(doc)
    }

    override suspend fun addPhotosToRemoteDoc(
        remoteId: Long,
        photos: List<Photo>,
        newJsonPages: String
    ) =
        remoteDatasource.addPhotosDoc(
            remoteId = remoteId,
            photos = photos,
            newJsonPages = newJsonPages
        )

    override suspend fun deleteDocPhotosFirebase(remoteId: Long, photo: Photo, jsonPages: String) =
        remoteDatasource.deleteDocPhotosFirebase(remoteId, photo, jsonPages)

    override suspend fun deleteDocFirebase(remoteId: Long, pages: List<Photo>) =
        remoteDatasource.deleteDocFirebase(remoteId, pages)

    override suspend fun getSynStrategy() = remoteDatasource.getSyncStrategy()

    override suspend fun syncData(customId: Long) = remoteDatasource.syncData(customId)

    override suspend fun insertDocs(docs: List<Doc>) {
        localDatasource.insertDocs(docs)
    }

    override suspend fun clearDocs() {
        localDatasource.clearDocs()
    }

    override suspend fun updateRemoteDocName(remoteId: Long, name: String) =
        remoteDatasource.updateDocNameFirebase(remoteId, name)

    override suspend fun updateRemoteDocPhoto(remoteId: Long, photo: Photo) =
        remoteDatasource.updateDocPhotoFirebase(remoteId, photo)

    override suspend fun uploadDoc(doc: Doc) =
        remoteDatasource.uploadDocFirebase(doc)

    override suspend fun sendCustomIdForceUpdate(customId: Long) {
        remoteDatasource.sendCustomIdForceUpdate(customId)
    }

    override suspend fun updateDocPhoto(localId: Long, photo: Photo) {
        localDatasource.updateDocPhoto(localId = localId, photo = photo)
    }
}