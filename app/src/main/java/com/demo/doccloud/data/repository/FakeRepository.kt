package com.demo.doccloud.data.repository

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import com.demo.doccloud.R
import com.demo.doccloud.data.datasource.local.room.entities.DatabaseDoc
import com.demo.doccloud.di.MainDispatcher
import com.demo.doccloud.domain.Doc
import com.demo.doccloud.domain.Photo
import com.demo.doccloud.domain.User
import com.demo.doccloud.utils.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.lang.Exception
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

    override suspend fun doLoginWithGoogle(data: Intent?) = runBlocking {
        if (shouldThrowNetworkingException) {
            return@runBlocking Result.error(context.getString(R.string.common_no_internet))
        }
        if (hasDelay) {
            delay(3000)
        }
        if (shouldReturnErrorOnLogin) {
            return@runBlocking Result.error(context.getString(R.string.login_error_api_google))
        }
        return@runBlocking Result.success(User("any", "any"))
    }


    override suspend fun getUser() = runBlocking {
        return@runBlocking Result.success(User("any", "any"))
    }

    override suspend fun doLogout() = runBlocking {
        if(shouldReturnErrorOnLogout){
            return@runBlocking Result.error(context.getString(R.string.login_revokeAccess), null)
        }else{
            return@runBlocking Result.success(true)
        }
    }

    override suspend fun saveDoc(doc: Doc): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteDoc(doc: Doc): Result<String> {
        TODO("Not yet implemented")
    }

    override suspend fun getDoc(id: Long): Result<Doc> {
        TODO("Not yet implemented")
    }

    override suspend fun updateDocName(
        localId: Long,
        remoteId: Long,
        name: String
    ){
        TODO("Not yet implemented")
    }

    override suspend fun deleteDocPhoto(localId: Long, remoteId: Long, photo: Photo) {
        TODO("Not yet implemented")
    }

    override suspend fun updateDocPhotos(
        localId: Long,
        remoteId: Long,
        photo: Photo
    ){
        TODO("Not yet implemented")
    }


}