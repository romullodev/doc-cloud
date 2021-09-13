package com.demo.doccloud.domain.usecases.contracts

import com.demo.doccloud.domain.entities.Photo

interface DeleteDocPhoto {
    suspend operator fun invoke(localId: Long, photo: Photo)
}