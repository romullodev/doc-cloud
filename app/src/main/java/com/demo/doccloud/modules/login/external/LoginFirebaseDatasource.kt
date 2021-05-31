package com.demo.doccloud.modules.login.external

import com.demo.doccloud.modules.login.domain.entities.User
import com.demo.doccloud.modules.login.infra.datasource.ILoginRemoteDataSource
import com.demo.doccloud.utils.Result

class LoginFirebaseDatasource() : ILoginRemoteDataSource {
    override suspend fun doLoginGmail(email: String, password: String): Result<User> {
        TODO("Not yet implemented")
    }

    override suspend fun doLoginEmail(email: String, password: String): Result<User> {
        TODO("Not yet implemented")
    }
}