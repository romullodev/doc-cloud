package com.demo.doccloud.domain.usecases.contracts

interface RemoveTempFile {
    suspend operator fun invoke(customId: Long)
}