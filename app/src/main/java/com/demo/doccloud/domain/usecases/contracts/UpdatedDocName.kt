package com.demo.doccloud.domain.usecases.contracts

interface UpdatedDocName {
    suspend operator fun invoke(localId: Long, remoteId: Long, name: String)
}