package com.demo.doccloud.domain.usecases.contracts

import com.demo.doccloud.domain.entities.Photo

interface UpdateRemoteDocPhoto {
    suspend operator fun invoke(remoteId: Long, photo: Photo)
}