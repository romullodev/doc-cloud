package com.demo.doccloud.domain.usecases.impl

import com.demo.doccloud.data.repository.Repository
import com.demo.doccloud.domain.entities.User
import com.demo.doccloud.domain.usecases.contracts.DoLoginByEmail
import com.demo.doccloud.domain.usecases.contracts.SaveCustomIdSyncStrategy
import com.demo.doccloud.domain.usecases.contracts.SendCustomIdAndForceUpdate
import javax.inject.Inject

class DoLoginByEmailImpl @Inject constructor(
    private val saveCustomIdSyncStrategy: SaveCustomIdSyncStrategy,
    private val sendCustomIdAndForceUpdate: SendCustomIdAndForceUpdate,
    private val repository: Repository
): DoLoginByEmail {
    override suspend fun invoke(email: String, password: String) : User {
        val user =  repository.doLoginByEmail(email, password)
        val id = saveCustomIdSyncStrategy()
        sendCustomIdAndForceUpdate(id)
        return user
    }
}