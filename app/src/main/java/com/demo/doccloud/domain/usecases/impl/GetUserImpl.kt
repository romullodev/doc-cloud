package com.demo.doccloud.domain.usecases.impl

import com.demo.doccloud.data.repository.Repository
import com.demo.doccloud.domain.usecases.contracts.GetUser
import javax.inject.Inject

class GetUserImpl @Inject constructor(
    private val repository: Repository
): GetUser {
    override suspend fun invoke() = repository.getUser()
}