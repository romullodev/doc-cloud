package com.demo.doccloud.domain.usecases.impl

import com.demo.doccloud.data.repository.Repository
import com.demo.doccloud.domain.entities.SignUpParams
import com.demo.doccloud.domain.entities.User
import com.demo.doccloud.domain.usecases.contracts.RegisterNewUserByEmail
import com.demo.doccloud.domain.usecases.contracts.SaveCustomIdSyncStrategy
import com.demo.doccloud.domain.usecases.contracts.SendCustomIdAndForceUpdate
import javax.inject.Inject

class RegisterNewUserByEmailImpl @Inject constructor(
    private val saveCustomIdSyncStrategy: SaveCustomIdSyncStrategy,
    private val sendCustomIdAndForceUpdate: SendCustomIdAndForceUpdate,
    private val repository: Repository
) : RegisterNewUserByEmail {
    override suspend fun invoke(params: SignUpParams): User {
        val user = repository.registerUser(params)
        val id = saveCustomIdSyncStrategy()
        sendCustomIdAndForceUpdate(id)
        return user
    }
}