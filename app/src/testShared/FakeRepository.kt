package com.demo.doccloud

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.demo.doccloud.data.repository.Repository
import com.demo.doccloud.di.MainDispatcher
import com.demo.doccloud.domain.entities.*
import com.demo.doccloud.idling.wrapEspressoIdlingResource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @MainDispatcher private val dispatcher: CoroutineDispatcher = Dispatchers.Main
) : Repository {

    private val localDocs =  ArrayList<Doc>()

    private var hasDelay = false
    private var shouldThrowNetworkingException = false
    private var shouldThrowApiException = false
    private var shouldThrowUserWithNoIdException = false
    private var shouldThrowUnknownException = false
    private var shouldThrowExceptionWhenGetUser = false
    private var shouldThrowExceptionWhenDeleteLocalDoc = false

    private var shouldReturnErrorOnLogout = false
    private val delayDuration = 1000L

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

    fun setHasDelay(value: Boolean) {
        this.hasDelay = value
    }

    fun clearFlags(){
        hasDelay = false
        shouldThrowNetworkingException = false
        shouldThrowApiException = false
        shouldThrowUserWithNoIdException = false
        shouldThrowUnknownException = false
        shouldThrowExceptionWhenGetUser = false
        shouldThrowExceptionWhenDeleteLocalDoc = false
        shouldReturnErrorOnLogout = false
    }

    private var _docs : MutableLiveData<List<Doc>> =
        if(localDocs.isEmpty()) {
            localDocs.addAll(listOf(
                fakeDoc.copy(localId = 1, name = "doc 1", pages = arrayListOf(Photo(id = 1, path = Uri.EMPTY.path!!))),
                fakeDoc.copy(localId = 2, name = "doc 2"))
            )
            MutableLiveData(localDocs)
        } else{
            MutableLiveData(localDocs)
        }

    override val docs: LiveData<List<Doc>>
        get() = _docs

    override suspend fun doLoginWithGoogle(data: Intent?): User{
        return wrapEspressoIdlingResource {
            return@wrapEspressoIdlingResource withContext(dispatcher) {
                //idlingResourceBooleanVersion.setIdleState(false)
                if (shouldThrowNetworkingException) {
                    throw Exception(context.getString(R.string.common_no_internet))
                }
                if (shouldThrowApiException) {
                    throw Exception(context.getString(R.string.login_error_api_google))
                }
                if (shouldThrowUserWithNoIdException) {
                    throw Exception(context.getString(R.string.login_user_with_no_id))
                }
                if (shouldThrowUnknownException) {
                    throw Exception(context.getString(R.string.common_unknown_error))
                }
                if(hasDelay)
                    delay(delayDuration)
                return@withContext User("any", "any")
            }
        }
        //idlingResourceBooleanVersion.setIdleState(true)
    }

    override suspend fun doLoginByEmail(email: String, password: String): User {
        TODO("Not yet implemented")
    }

    override suspend fun registerUser(params: SignUpParams): User {
        return wrapEspressoIdlingResource {
            return@wrapEspressoIdlingResource withContext(dispatcher){
                if(hasDelay){
                    delay(delayDuration)
                }
                return@withContext User(
                    displayName = "any",
                    userId = "any"
                )
            }
        }
    }

    override suspend fun recoverPassword(email: String) {
        TODO("Not yet implemented")
    }

    override suspend fun doLogout(){
        wrapEspressoIdlingResource {
            withContext(dispatcher) {
                if(hasDelay)
                    delay(delayDuration)
            }

        }
        //do nothing
    }
    override suspend fun getUser(): User {
        return wrapEspressoIdlingResource {
            return@wrapEspressoIdlingResource withContext(dispatcher){
                if(shouldThrowExceptionWhenGetUser){
                    throw Exception()
                }
                return@withContext User(
                    displayName = "any",
                    userId = "any"
                )
            }
        }
    }


    override suspend fun saveDoc(doc: Doc): Long{
        return wrapEspressoIdlingResource {
            return@wrapEspressoIdlingResource withContext(dispatcher){
                if(shouldThrowUnknownException){
                    throw Exception()
                }
                if(hasDelay){
                    delay(delayDuration)
                }
                localDocs.add(doc)
                _docs.value = localDocs
                return@withContext -1L
            }
        }
    }

    override suspend fun deleteDoc(doc: Doc) {
        runBlocking {
            if(shouldThrowExceptionWhenDeleteLocalDoc){
                throw Exception()
            }
            localDocs.remove(doc)
        }
    }

    override suspend fun getDoc(id: Long): Doc{
        return wrapEspressoIdlingResource {
            return@wrapEspressoIdlingResource withContext(dispatcher){
                return@withContext localDocs.find { it.localId == id }!!
            }
        }
    }

    override suspend fun updateDocPhoto(localId: Long, photo: Photo){
        wrapEspressoIdlingResource {
            withContext(dispatcher){
                if(shouldThrowUnknownException){
                    throw Exception()
                }
            }
        }
    }

    override suspend fun updateDocName(localId: Long, name: String){
        wrapEspressoIdlingResource {
            withContext(dispatcher){
                if(shouldThrowUnknownException){
                    throw Exception(context.getString(R.string.common_unknown_error))
                }
            }
        }

    }

    override suspend fun deleteDocPhoto(localId: Long, photo: Photo){
        wrapEspressoIdlingResource {
            withContext(dispatcher){
                if(shouldThrowUnknownException){
                    throw Exception()
                }
                val doc: Doc = localDocs.find { it.localId == localId }!!
                val pages = ArrayList(doc.pages)
                pages.remove(photo)
                localDocs.remove(doc)
                localDocs.add(doc.copy(pages = pages))
                _docs.value = localDocs
            }
        }
    }

    override suspend fun scheduleToSyncData() {
        TODO("Not yet implemented")
    }

    override suspend fun addPhotos(localId: Long, photos: List<Photo>){
        wrapEspressoIdlingResource {
            withContext(dispatcher){
                if(shouldThrowUnknownException){
                    throw Exception()
                }
                val doc: Doc = localDocs.find { it.localId == localId }!!
                localDocs.remove(doc)
                localDocs.add(doc.copy(pages = photos))
                _docs.value = localDocs
            }
        }
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

    override suspend fun sendCustomIdForceUpdate(customId: Long) {
        wrapEspressoIdlingResource {
            withContext(dispatcher){
                if(hasDelay)
                    delay(delayDuration)
            }
        }
    }

    override suspend fun generatePDFLink(file: File, customId: Long): Uri {
        TODO("Not yet implemented")
    }

    override suspend fun removeTempFile(customId: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun getRemoveTempFileTime(): Long {
        TODO("Not yet implemented")
    }


    companion object{
        val fakeDoc = Doc(
            remoteId = -1L,
            name = "any name",
            date = "18/09/2021",
            pages = listOf(),
            status = DocStatus.NOT_SENT,
        )
    }

}