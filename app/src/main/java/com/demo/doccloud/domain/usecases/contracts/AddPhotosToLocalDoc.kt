package com.demo.doccloud.domain.usecases.contracts

import com.demo.doccloud.domain.entities.Photo

interface AddPhotosToLocalDoc {
    suspend operator fun invoke(localId: Long, photos: List<Photo>)
}