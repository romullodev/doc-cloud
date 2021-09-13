package com.demo.doccloud.domain.usecases.impl

import com.demo.doccloud.data.repository.Repository
import com.demo.doccloud.domain.usecases.contracts.GetSyncStrategy
import javax.inject.Inject

class GetSyncStrategyImpl @Inject constructor(
    private val repository: Repository
) : GetSyncStrategy {
    override suspend fun invoke() = repository.getSynStrategy()
}