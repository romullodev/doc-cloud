package com.demo.doccloud.data.repository

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import com.demo.doccloud.R
import com.demo.doccloud.domain.entities.Doc
import com.demo.doccloud.domain.entities.Photo
import com.demo.doccloud.domain.entities.SyncStrategy
import com.demo.doccloud.domain.entities.User
import com.demo.doccloud.utils.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : Repository {

    private var hasDelay = false
    private var shouldThrowNetworkingException = false
    private var shouldReturnErrorOnLogin = false
    private var shouldReturnErrorOnLogout = false
    private var delayDuration = 0L

    fun setShouldThrowNetworkingException(value: Boolean) {
        this.shouldThrowNetworkingException = value
    }

    fun setShouldReturnErrorOnLogin(value: Boolean) {
        this.shouldReturnErrorOnLogin = value
    }

    fun setShouldReturnErrorOnLogout(value: Boolean) {
        this.shouldReturnErrorOnLogout = value
    }

    fun setHasDelay(value: Boolean, duration: Long) {
        this.hasDelay = true
        this.delayDuration = duration
    }

    override val docs: LiveData<List<Doc>>
        get() = TODO("Not yet implemented")

    override suspend fun doLoginWithGoogle(data: Intent?, customId: Long): User {
        TODO("Not yet implemented")
    }

//    override suspend fun doLoginWithGoogle(data: Intent?) = runBlocking {
//        if (shouldThrowNetworkingException) {
//            return@runBlocking Result.error(context.getString(R.string.common_no_internet))
//        }
//        if (hasDelay) {
//            delay(3000)
//        }
//        if (shouldReturnErrorOnLogin) {
//            return@runBlocking Result.error(context.getString(R.string.login_error_api_google))
//        }
//        return@runBlocking Result.success(User("any", "any"))
//    }

    override suspend fun getUser(): User {
        TODO("Not yet implemented")
    }


//    override suspend fun getUser() = runBlocking {
//        return@runBlocking Result.success(User("any", "any"))
//    }

    override suspend fun doLogout() = runBlocking {
        if(shouldReturnErrorOnLogout){
            return@runBlocking
        }else{
            return@runBlocking
        }
    }

    override suspend fun saveDoc(doc: Doc): Long {
        TODO("Not yet implemented")
    }

    override suspend fun deleteDoc(doc: Doc) {
        TODO("Not yet implemented")
    }

    override suspend fun getDoc(id: Long): Doc {
        TODO("Not yet implemented")
    }

    override suspend fun updateDocPhoto(localId: Long, photo: Photo) {
        TODO("Not yet implemented")
    }

    override suspend fun updateDocName(localId: Long, name: String) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteDocPhoto(localId: Long, photo: Photo) {
        TODO("Not yet implemented")
    }

    override suspend fun scheduleToSyncData() {
        TODO("Not yet implemented")
    }

    override suspend fun addPhotos(localId: Long, photos: List<Photo>) {
        TODO("Not yet implemented")
    }

    override suspend fun saveLong(key: String, value: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun getLong(key: String, defaultValue: Long): Long {
        TODO("Not yet implemented")
    }

    override suspend fun updateLocalDoc(doc: Doc) {
        TODO("Not yet implemented")
    }

    override suspend fun addPhotosToRemoteDoc(
        remoteId: Long,
        photos: List<Photo>,
        newJsonPages: String
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteDocPhotosFirebase(remoteId: Long, photo: Photo, jsonPages: String) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteDocFirebase(remoteId: Long, pages: List<Photo>) {
        TODO("Not yet implemented")
    }

    override suspend fun getSynStrategy(): SyncStrategy {
        TODO("Not yet implemented")
    }

    override suspend fun syncData(customId: Long): List<Doc> {
        TODO("Not yet implemented")
    }

    override suspend fun insertDocs(docs: List<Doc>) {
        TODO("Not yet implemented")
    }

    override suspend fun clearDocs() {
        TODO("Not yet implemented")
    }

    override suspend fun updateRemoteDocName(remoteId: Long, name: String) {
        TODO("Not yet implemented")
    }

    override suspend fun updateRemoteDocPhotos(remoteId: Long, photo: Photo) {
        TODO("Not yet implemented")
    }

    override suspend fun uploadDoc(doc: Doc) {
        TODO("Not yet implemented")
    }


}