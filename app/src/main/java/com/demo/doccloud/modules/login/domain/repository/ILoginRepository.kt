package com.demo.doccloud.modules.login.domain.repository

import com.demo.doccloud.modules.login.domain.entities.User
import com.demo.doccloud.utils.Result

interface ILoginRepository {
    suspend fun doLoginEmail(email: String, password: String): Result<User>
    suspend fun doLoginGmail(email: String, password: String): Result<User>
}