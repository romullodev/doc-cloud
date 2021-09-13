package com.demo.doccloud.domain.usecases.contracts

interface SaveCustomIdSyncStrategy {
    suspend operator fun invoke() : Long
}