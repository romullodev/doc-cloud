package com.demo.doccloud.domain.usecases.impl

import com.demo.doccloud.data.repository.Repository
import com.demo.doccloud.domain.usecases.contracts.SaveCustomIdSyncStrategy
import com.demo.doccloud.utils.AppConstants
import javax.inject.Inject

class SaveCustomIdSyncStrategyImpl @Inject constructor(
    private val repository: Repository
): SaveCustomIdSyncStrategy {
    override suspend fun invoke() : Long{
        val id = System.currentTimeMillis()
        repository.saveLong(key = AppConstants.LOCAL_DATABASE_CUSTOM_ID_KEY, value = id)
        return id
    }
}