package com.demo.doccloud.domain.usecases.contracts

interface SendCustomIdAndForceUpdate {
    suspend operator fun invoke(customId: Long)
}