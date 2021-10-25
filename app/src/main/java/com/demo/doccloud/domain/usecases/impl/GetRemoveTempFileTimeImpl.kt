package com.demo.doccloud.domain.usecases.impl

import com.demo.doccloud.data.repository.Repository
import com.demo.doccloud.domain.usecases.contracts.GetRemoveTempFileTime
import javax.inject.Inject

class GetRemoveTempFileTimeImpl @Inject constructor(
    private val repository: Repository
) : GetRemoveTempFileTime {
    override suspend fun invoke(): Long {
        return repository.getRemoveTempFileTime()
    }
}