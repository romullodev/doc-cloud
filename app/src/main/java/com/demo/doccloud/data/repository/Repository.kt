package com.demo.doccloud.data.repository

import android.content.Intent
import androidx.lifecycle.LiveData
import com.demo.doccloud.domain.entities.Doc
import com.demo.doccloud.domain.entities.Photo
import com.demo.doccloud.domain.entities.SyncStrategy
import com.demo.doccloud.domain.entities.User
import com.demo.doccloud.utils.Result
import java.io.File

interface Repository {
    //A pointer to database for retrieving data when change automatically
    val docs: LiveData<List<Doc>>

    suspend fun doLoginWithGoogle(data: Intent?, customId: Long) : User
    suspend fun getUser() : User
    suspend fun doLogout()
    suspend fun saveDoc(doc: Doc) : Long
    suspend fun deleteDoc(doc: Doc)
    suspend fun getDoc(id: Long) : Doc
    suspend fun updateDocPhoto(localId: Long, photo: Photo)
    suspend fun updateDocName(localId: Long, name: String)
    suspend fun deleteDocPhoto(localId: Long, photo: Photo)
    suspend fun scheduleToSyncData()
    suspend fun addPhotos(localId: Long, photos: List<Photo>)
    suspend fun saveLong(key: String, value: Long)
    suspend fun getLong(key: String, defaultValue: Long) : Long
    suspend fun updateLocalDoc(doc: Doc)
    suspend fun addPhotosToRemoteDoc(remoteId: Long, photos: List<Photo>, newJsonPages: String)
    suspend fun deleteDocPhotosFirebase(remoteId: Long, photo: Photo, jsonPages: String)
    suspend fun deleteDocFirebase(remoteId: Long, pages: List<Photo>)
    suspend fun getSynStrategy(): SyncStrategy
    suspend fun syncData(customId: Long): List<Doc>
    suspend fun insertDocs(docs: List<Doc>)
    suspend fun clearDocs()
    suspend fun updateRemoteDocName(remoteId: Long, name: String)
    suspend fun updateRemoteDocPhoto(remoteId: Long, photo: Photo)
    suspend fun uploadDoc(doc: Doc)
}