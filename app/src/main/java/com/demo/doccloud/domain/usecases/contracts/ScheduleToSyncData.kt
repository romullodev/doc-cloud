package com.demo.doccloud.domain.usecases.contracts

interface ScheduleToSyncData {
    suspend operator fun invoke()
}