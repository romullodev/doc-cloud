package com.demo.doccloud.domain.usecases.contracts

import com.demo.doccloud.domain.entities.Photo

interface DeleteRemoteDoc {
    suspend operator fun invoke(remoteId: Long, pages: List<Photo>)
}