package com.demo.doccloud.domain.usecases.contracts

interface ScheduleToUpdateRemoteDocName {
    suspend operator fun invoke(localId: Long, remoteId: Long, name: String)
}