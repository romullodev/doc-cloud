package com.demo.doccloud.modules.login.domain.usecase

import com.demo.doccloud.modules.login.domain.entities.User
import com.demo.doccloud.modules.login.domain.repository.ILoginRepository
import com.demo.doccloud.utils.Result

interface IDoLoginWithEmail {
    suspend fun doLoginEmail(email: String, password: String): Result<User>
}


class DoLoginWithEmail(private val repository: ILoginRepository) : IDoLoginWithEmail {
    override suspend fun doLoginEmail(email: String, password: String): Result<User> {
        return repository.doLoginEmail(email, password)
    }
}