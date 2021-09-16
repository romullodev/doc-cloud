package com.demo.doccloud.data.datasource.remote

import android.content.Intent
import com.demo.doccloud.domain.entities.Doc
import com.demo.doccloud.domain.entities.Photo
import com.demo.doccloud.domain.entities.SyncStrategy
import com.demo.doccloud.domain.entities.User
import com.demo.doccloud.utils.Result

interface RemoteDataSource {
    suspend fun doLoginWithGoogle(data: Intent?, customId: Long) : User
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
}