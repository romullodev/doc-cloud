package com.demo.doccloud.domain.usecases.contracts

import com.demo.doccloud.domain.entities.Photo

interface AddPhotos {
    suspend operator fun invoke(localId: Long, photos: List<Photo>)
}