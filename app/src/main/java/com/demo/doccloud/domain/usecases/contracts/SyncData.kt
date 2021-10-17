package com.demo.doccloud.domain.usecases.contracts

import com.demo.doccloud.domain.entities.Doc

interface SyncData {
    suspend operator fun invoke(customId: Long)
}