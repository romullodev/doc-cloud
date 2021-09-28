package com.demo.doccloud.domain.usecases.contracts

import com.demo.doccloud.domain.entities.User

interface DoLoginByEmail {
    suspend operator fun invoke(email: String, password: String) : User
}