package com.demo.doccloud.data.datasource.remote

import android.content.Intent
import com.demo.doccloud.domain.User
import com.demo.doccloud.utils.Result

interface RemoteDataSource {
    suspend fun doLoginWithGoogle(data: Intent?) : Result<User>
    suspend fun getUser() : Result<User>
    suspend fun doLogout() : Result<Boolean>
}