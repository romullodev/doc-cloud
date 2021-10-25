package com.demo.doccloud.domain.usecases.contracts

interface ScheduleToRemoveTempFile {
    suspend operator fun invoke(customId: Long, delay: Long)
}