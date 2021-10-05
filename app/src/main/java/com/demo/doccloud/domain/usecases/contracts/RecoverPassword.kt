package com.demo.doccloud.domain.usecases.contracts

interface RecoverPassword {
    suspend operator fun invoke(email: String)
}