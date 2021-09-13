package com.demo.doccloud.domain.usecases.contracts

interface ScheduleToAddRemoteDocPhotos {
    suspend operator fun invoke(localId: Long, photosId: List<Long>)
}