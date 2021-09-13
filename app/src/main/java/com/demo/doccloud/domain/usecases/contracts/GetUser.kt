package com.demo.doccloud.domain.usecases.contracts

import com.demo.doccloud.domain.entities.User

interface GetUser {
    suspend operator fun invoke() : User
}