package com.demo.doccloud.modules.login.infra.repositories

import com.demo.doccloud.modules.login.domain.entities.User
import com.demo.doccloud.modules.login.domain.repository.ILoginRepository
import com.demo.doccloud.modules.login.infra.datasource.ILoginRemoteDataSource
import com.demo.doccloud.utils.Result

class LoginRepository(private val remoteDataSource: ILoginRemoteDataSource,): ILoginRepository {
    override suspend fun doLoginEmail(email: String, password: String): Result<User> {
        return remoteDataSource.doLoginEmail(email, password)
    }

    override suspend fun doLoginGmail(email: String, password: String): Result<User> {
        return remoteDataSource.doLoginGmail(email, password)
    }
}