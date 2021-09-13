package com.demo.doccloud.domain.usecases.contracts

import com.demo.doccloud.domain.entities.Photo

interface AddPhotoToRemoteDoc {
    suspend operator fun invoke(remoteId: Long, photos: List<Photo>, newJsonPages: String)
}