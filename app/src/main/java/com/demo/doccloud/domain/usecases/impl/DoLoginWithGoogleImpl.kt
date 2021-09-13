package com.demo.doccloud.domain.usecases.impl

import android.content.Intent
import com.demo.doccloud.data.repository.Repository
import com.demo.doccloud.domain.entities.User
import com.demo.doccloud.domain.usecases.contracts.DoLoginWithGoogle
import com.demo.doccloud.domain.usecases.contracts.SaveCustomIdSyncStrategy
import com.demo.doccloud.utils.AppConstants
import javax.inject.Inject

class DoLoginWithGoogleImpl @Inject constructor(
    private val saveCustomIdSyncStrategy: SaveCustomIdSyncStrategy,
    private val repository: Repository
): DoLoginWithGoogle {
    override suspend fun invoke(intent: Intent?): User {
        val id = saveCustomIdSyncStrategy()
        return repository.doLoginWithGoogle(intent, id)
    }
}