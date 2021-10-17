package com.demo.doccloud.domain.usecases.contracts

interface GetSavedCustomIdSyncStrategy {
    suspend operator fun invoke(): Long
}