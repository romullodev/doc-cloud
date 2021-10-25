package com.demo.doccloud.domain.usecases.contracts

interface GetRemoveTempFileTime {
    suspend operator fun invoke(): Long
}