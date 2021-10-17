package com.demo.doccloud.domain.usecases.contracts

interface ScheduleToDeleteRemoteDoc {
    suspend operator fun invoke(remoteId: Long, jsonPages: String)
}