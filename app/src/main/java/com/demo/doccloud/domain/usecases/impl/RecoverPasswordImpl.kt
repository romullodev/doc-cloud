package com.demo.doccloud.domain.usecases.impl

import com.demo.doccloud.data.repository.Repository
import com.demo.doccloud.domain.usecases.contracts.RecoverPassword
import javax.inject.Inject

class RecoverPasswordImpl @Inject constructor(
    private val repository: Repository
): RecoverPassword {
    override suspend fun invoke(email: String) {
        repository.recoverPassword(email)
    }
}