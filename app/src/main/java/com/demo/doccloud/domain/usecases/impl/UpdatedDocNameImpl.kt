package com.demo.doccloud.domain.usecases.impl

import com.demo.doccloud.domain.usecases.contracts.ScheduleToUpdateRemoteDocName
import com.demo.doccloud.domain.usecases.contracts.UpdateLocalDocName
import com.demo.doccloud.domain.usecases.contracts.UpdatedDocName
import javax.inject.Inject

class UpdatedDocNameImpl @Inject constructor(
    private val scheduleToUpdateDocNameUseCase: ScheduleToUpdateRemoteDocName,
    private val updateDocNameOnDeviceUseCase: UpdateLocalDocName
): UpdatedDocName {
    override suspend fun invoke(localId: Long, remoteId: Long, name: String) {
        updateDocNameOnDeviceUseCase(localId = localId, name = name)
        scheduleToUpdateDocNameUseCase(localId = localId, name = name, remoteId = remoteId)
    }
}