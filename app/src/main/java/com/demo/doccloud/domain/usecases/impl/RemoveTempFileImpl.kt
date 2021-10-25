package com.demo.doccloud.domain.usecases.impl

import com.demo.doccloud.data.repository.Repository
import com.demo.doccloud.domain.usecases.contracts.RemoveTempFile
import javax.inject.Inject

class RemoveTempFileImpl @Inject constructor(
    private val repository: Repository
): RemoveTempFile {
    override suspend fun invoke(customId: Long) {
        repository.removeTempFile(customId)
    }
}