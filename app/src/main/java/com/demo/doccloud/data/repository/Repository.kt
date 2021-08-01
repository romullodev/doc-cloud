package com.demo.doccloud.data.repository

import android.content.Intent
import com.demo.doccloud.domain.User
import com.demo.doccloud.utils.Result

interface Repository {
    suspend fun doLoginWithGoogle(data: Intent?) : Result<User>
    suspend fun getUser() : Result<User>
    suspend fun doLogout() : Result<Boolean>
}