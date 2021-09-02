package com.demo.doccloud.data.datasource.remote

import android.content.Intent
import com.demo.doccloud.domain.Doc
import com.demo.doccloud.domain.Photo
import com.demo.doccloud.domain.SyncStrategy
import com.demo.doccloud.domain.User
import com.demo.doccloud.utils.Result

interface RemoteDataSource {
    suspend fun doLoginWithGoogle(data: Intent?, customId: Long) : Result<User>
    suspend fun getUser() : Result<User>
    suspend fun doLogout() : Result<Boolean>
    suspend fun uploadDocFirebase(doc: Doc)
    suspend fun deleteDocFirebase(remoteId: Long, pages: List<Photo>)
    suspend fun updateDocNameFirebase(remoteId: Long, name: String) : Result<Boolean>
    suspend fun updateDocPhotosFirebase(remoteId: Long, photo: Photo) : Result<Boolean>
    suspend fun deleteDocPhotosFirebase(remoteId: Long, photo: Photo, jsonPages: String) : Result<Boolean>
    suspend fun syncData(customId: Long): Result<List<Doc>>
    suspend fun getSyncStrategy(): Result<SyncStrategy>
    suspend fun addPhotosDoc(remoteId: Long, photos: List<Photo>, newJsonPages: String): Result<Boolean>
}