package com.demo.doccloud.data.datasource.remote

import android.content.Intent
import com.demo.doccloud.domain.entities.*
import com.demo.doccloud.utils.Result

interface RemoteDataSource {
    suspend fun doLoginWithGoogle(data: Intent?) : User
    suspend fun doLoginByEmail(email: String, password: String) : User
    suspend fun registerUser(params: SignUpParams) : User
    suspend fun getUser() : User
    suspend fun doLogout()
    suspend fun uploadDocFirebase(doc: Doc)
    suspend fun deleteDocFirebase(remoteId: Long, pages: List<Photo>)
    suspend fun updateDocNameFirebase(remoteId: Long, name: String)
    suspend fun updateDocPhotoFirebase(remoteId: Long, photo: Photo)
    suspend fun deleteDocPhotosFirebase(remoteId: Long, photo: Photo, jsonPages: String)
    suspend fun syncData(customId: Long): List<Doc>
    suspend fun getSyncStrategy() : SyncStrategy
    suspend fun addPhotosDoc(remoteId: Long, photos: List<Photo>, newJsonPages: String)
    suspend fun sendCustomIdForceUpdate(customId: Long)
}