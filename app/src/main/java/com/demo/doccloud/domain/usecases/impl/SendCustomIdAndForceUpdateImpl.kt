package com.demo.doccloud.domain.usecases.impl

import com.demo.doccloud.data.repository.Repository
import com.demo.doccloud.domain.usecases.contracts.SendCustomIdAndForceUpdate
import javax.inject.Inject

class SendCustomIdAndForceUpdateImpl @Inject constructor(
    private val repository: Repository
): SendCustomIdAndForceUpdate {
    override suspend fun invoke(customId: Long) {
        repository.sendCustomIdForceUpdate(customId)
    }
}