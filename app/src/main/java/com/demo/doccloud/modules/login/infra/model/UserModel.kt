package com.demo.doccloud.modules.login.infra.model

import com.demo.doccloud.modules.login.domain.entities.User

data class UserModel (
    val displayName: String,
    val userId: String,
)

fun UserModel.asDomainModel(): User {
    return User(
        displayName = this.displayName,
        userId = this.userId,
    )
}