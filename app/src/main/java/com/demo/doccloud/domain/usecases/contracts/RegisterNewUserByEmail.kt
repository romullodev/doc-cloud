package com.demo.doccloud.domain.usecases.contracts

import com.demo.doccloud.domain.entities.SignUpParams
import com.demo.doccloud.domain.entities.User

interface RegisterNewUserByEmail {
    suspend operator fun invoke(params: SignUpParams) : User
}