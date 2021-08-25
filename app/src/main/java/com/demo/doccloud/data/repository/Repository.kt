package com.demo.doccloud.data.repository

import android.content.Intent
import androidx.lifecycle.LiveData
import com.demo.doccloud.data.datasource.local.room.entities.DatabaseDoc
import com.demo.doccloud.domain.Doc
import com.demo.doccloud.domain.User
import com.demo.doccloud.utils.Result

interface Repository {
    //A pointer to database for retrieving data when change automatically
    val docs: LiveData<List<Doc>>

    suspend fun doLoginWithGoogle(data: Intent?) : Result<User>
    suspend fun getUser() : Result<User>
    suspend fun doLogout() : Result<Boolean>
    suspend fun saveDoc(doc: Doc) : Result<Boolean>
    suspend fun deleteDoc(doc: Doc) : Result<String>
}