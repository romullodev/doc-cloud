package com.demo.doccloud.domain.usecases.impl

import com.demo.doccloud.data.repository.Repository
import com.demo.doccloud.domain.usecases.contracts.GetSavedCustomIdSyncStrategy
import com.demo.doccloud.utils.AppConstants
import javax.inject.Inject

class GetSavedCustomIdSyncStrategyImpl @Inject constructor(
    private val repository: Repository
) : GetSavedCustomIdSyncStrategy {
    override suspend fun invoke(): Long = repository.getLong(
        key = AppConstants.LOCAL_DATABASE_CUSTOM_ID_KEY,
        defaultValue = AppConstants.DATABASE_DEFAULT_CUSTOM_ID
    )
}