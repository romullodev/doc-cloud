package com.demo.doccloud.data.repository

import android.content.Intent
import androidx.lifecycle.LiveData
import com.demo.doccloud.data.datasource.local.room.entities.DatabaseDoc
import com.demo.doccloud.domain.Doc
import com.demo.doccloud.domain.Photo
import com.demo.doccloud.domain.User
import com.demo.doccloud.utils.Result
import java.io.File

interface Repository {
    //A pointer to database for retrieving data when change automatically
    val docs: LiveData<List<Doc>>

    suspend fun doLoginWithGoogle(data: Intent?) : Result<User>
    suspend fun getUser() : Result<User>
    suspend fun doLogout() : Result<Boolean>
    suspend fun saveDoc(doc: Doc) : Result<Boolean>
    suspend fun deleteDoc(doc: Doc) : Result<String>
    suspend fun getDoc(id: Long) : Result<Doc>
    suspend fun updateDocPhotos(localId: Long, remoteId: Long, photo: Photo)
    suspend fun updateDocName(localId: Long, remoteId: Long, name: String)
    suspend fun deleteDocPhoto(localId: Long, remoteId: Long, photo: Photo)
    suspend fun scheduleToSyncData()
    suspend fun generatePdf(doc: Doc): Result<File>
}