package com.demo.doccloud.domain.usecases.contracts

import com.demo.doccloud.domain.entities.SyncStrategy

interface GetSyncStrategy {
    suspend operator fun invoke(): SyncStrategy
}