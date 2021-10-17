package com.demo.doccloud.domain.usecases.impl

import com.demo.doccloud.data.repository.Repository
import com.demo.doccloud.domain.usecases.contracts.GetDocById
import javax.inject.Inject

class GetDocByIdImpl @Inject constructor(
    private val repository: Repository
) : GetDocById {
    override suspend fun invoke(id: Long) = repository.getDoc(id)
}