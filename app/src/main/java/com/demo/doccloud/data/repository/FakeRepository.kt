package com.demo.doccloud.data.repository

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.demo.doccloud.R
import com.demo.doccloud.domain.entities.*
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
    private var shouldThrowApiException = false
    private var shouldThrowUserWithNoIdException = false
    private var shouldThrowUnknownException = false
    private var shouldThrowExceptionWhenGetUser = false
    private var shouldThrowExceptionWhenDeleteLocalDoc = false

    private var shouldReturnErrorOnLogout = false
    private var delayDuration = 0L

    fun setShouldThrowNetworkingException(value: Boolean) {
        this.shouldThrowNetworkingException = value
    }
    fun setShouldThrowApiException(value: Boolean) {
        this.shouldThrowApiException = value
    }
    fun setShouldThrowUserWithNoIdException(value: Boolean) {
        this.shouldThrowUserWithNoIdException = value
    }
    fun setShouldThrowUnknownException(value: Boolean){
        this.shouldThrowUnknownException = value
    }
    fun setShouldThrowExceptionWhenGetUser(value: Boolean){
        this.shouldThrowExceptionWhenGetUser = value
    }
    fun setShouldThrowExceptionWhenDeleteLocalDoc(value: Boolean){
        this.shouldThrowExceptionWhenDeleteLocalDoc = value
    }

    fun setShouldReturnErrorOnLogout(value: Boolean) {
        this.shouldReturnErrorOnLogout = value
    }

    fun setHasDelay(value: Boolean, duration: Long) {
        this.hasDelay = true
        this.delayDuration = duration
    }

    override val docs: LiveData<List<Doc>>
        get() = MutableLiveData(listOf())

    override suspend fun doLoginWithGoogle(data: Intent?, customId: Long) = runBlocking {
        if(shouldThrowNetworkingException){
            throw Exception(context.getString(R.string.common_no_internet))
        }
        if(shouldThrowApiException){
            throw Exception(context.getString(R.string.login_error_api_google))
        }
        if(shouldThrowUserWithNoIdException){
            throw Exception(context.getString(R.string.login_user_with_no_id))
        }
        if(shouldThrowUnknownException){
            throw Exception(context.getString(R.string.common_unknown_error))
        }

        return@runBlocking User("any", "any")

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

    override suspend fun getUser() = runBlocking {
        if(shouldThrowExceptionWhenGetUser){
            throw Exception()
        }
        return@runBlocking User(
            displayName = "any",
            userId = "any"
        )
    }


//    override suspend fun getUser() = runBlocking {
//        return@runBlocking Result.success(User("any", "any"))
//    }

    override suspend fun doLogout() = runBlocking {
        //do nothing
    }

    override suspend fun saveDoc(doc: Doc) = runBlocking {
        if(shouldThrowUnknownException){
            throw Exception()
        }
        return@runBlocking -1L
    }

    override suspend fun deleteDoc(doc: Doc) = runBlocking {
        if(shouldThrowExceptionWhenDeleteLocalDoc){
            throw Exception()
        }
        //do nothing
    }

    override suspend fun getDoc(id: Long) = runBlocking {
        return@runBlocking fakeDoc
    }

    override suspend fun updateDocPhoto(localId: Long, photo: Photo) = runBlocking {
        //do nothing
    }

    override suspend fun updateDocName(localId: Long, name: String) = runBlocking {
        if(shouldThrowUnknownException){
            throw Exception(context.getString(R.string.common_unknown_error))
        }
        //do nothing
    }

    override suspend fun deleteDocPhoto(localId: Long, photo: Photo) = runBlocking {
        if(shouldThrowUnknownException){
            throw Exception()
        }
        //do nothing
    }

    override suspend fun scheduleToSyncData() {
        TODO("Not yet implemented")
    }

    override suspend fun addPhotos(localId: Long, photos: List<Photo>)= runBlocking {
        if(shouldThrowUnknownException){
            throw Exception()
        }
        //do nothing
    }

    override suspend fun saveLong(key: String, value: Long) = runBlocking {
        //do nothing
    }

    override suspend fun getLong(key: String, defaultValue: Long) = runBlocking {
        return@runBlocking -1L
    }

    override suspend fun updateLocalDoc(doc: Doc)= runBlocking {
        //do nothing
    }

    override suspend fun addPhotosToRemoteDoc(
        remoteId: Long,
        photos: List<Photo>,
        newJsonPages: String
    ) = runBlocking {
        // do nothing
    }

    override suspend fun deleteDocPhotosFirebase(remoteId: Long, photo: Photo, jsonPages: String) = runBlocking {
        //do nothing
    }

    override suspend fun deleteDocFirebase(remoteId: Long, pages: List<Photo>) = runBlocking {
        //to nothing
    }

    override suspend fun getSynStrategy() = runBlocking {
        return@runBlocking SyncStrategy(
            expiration = -1L,
            lastUpdated = -1L,
            customId = -1L,
        )
    }

    override suspend fun syncData(customId: Long) = runBlocking {
        return@runBlocking listOf<Doc>()
    }

    override suspend fun insertDocs(docs: List<Doc>) = runBlocking {
        //do nothing
    }

    override suspend fun clearDocs() = runBlocking {
        //do nothing
    }

    override suspend fun updateRemoteDocName(remoteId: Long, name: String) = runBlocking {
        //do nothing
    }

    override suspend fun updateRemoteDocPhoto(remoteId: Long, photo: Photo) = runBlocking {
        // do nothing
    }

    override suspend fun uploadDoc(doc: Doc) = runBlocking {
        // do nothing
    }

    companion object{
        val fakeDoc = Doc(
            remoteId = -1L,
            name = "any name",
            date = "",
            pages = listOf(),
            status = DocStatus.NOT_SENT,
        )
    }


}