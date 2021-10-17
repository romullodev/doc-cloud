package com.demo.doccloud.domain.usecases.impl

import com.demo.doccloud.data.repository.Repository
import com.demo.doccloud.domain.usecases.contracts.DoLogout
import javax.inject.Inject

class DoLogoutImpl @Inject constructor(
    private val repository: Repository
): DoLogout {
    override suspend fun invoke() = repository.doLogout()
}