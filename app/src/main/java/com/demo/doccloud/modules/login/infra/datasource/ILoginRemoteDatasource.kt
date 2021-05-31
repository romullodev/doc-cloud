package com.demo.doccloud.modules.login.infra.datasource

import com.demo.doccloud.modules.login.domain.entities.User
import com.demo.doccloud.utils.Result

interface ILoginRemoteDataSource {
    suspend fun doLoginGmail(email: String, password: String): Result<User>
    suspend fun doLoginEmail(email: String, password: String): Result<User>
}