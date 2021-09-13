package com.demo.doccloud.domain.usecases.contracts

interface UpdateRemoteDocName {
    suspend operator fun invoke(remoteId: Long, name: String)
}