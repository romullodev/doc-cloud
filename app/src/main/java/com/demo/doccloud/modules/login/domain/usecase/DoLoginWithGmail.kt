package com.demo.doccloud.modules.login.domain.usecase

import com.demo.doccloud.modules.login.domain.entities.User
import com.demo.doccloud.modules.login.domain.repository.ILoginRepository
import com.demo.doccloud.utils.Result

interface IDoLoginWithGmail {
    suspend fun doLoginEmailGmail(email: String, password: String): Result<User>
}

class DoLoginWithGmail(private val repository: ILoginRepository) : IDoLoginWithEmail {
    override suspend fun doLoginEmail(email: String, password: String): Result<User> {
        return repository.doLoginGmail(email, password)
    }
}