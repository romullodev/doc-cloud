package com.demo.doccloud.domain.usecases.contracts

interface ScheduleToSaveRemoteDoc {
    suspend operator fun invoke(rowNumber: Long)
}