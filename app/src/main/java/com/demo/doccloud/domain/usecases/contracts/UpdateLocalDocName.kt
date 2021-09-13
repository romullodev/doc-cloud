package com.demo.doccloud.domain.usecases.contracts

interface UpdateLocalDocName {
    suspend operator fun invoke(localId: Long, name: String)
}