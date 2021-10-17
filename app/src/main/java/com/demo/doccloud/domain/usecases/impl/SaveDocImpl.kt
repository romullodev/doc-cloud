package com.demo.doccloud.domain.usecases.impl

import com.demo.doccloud.domain.entities.Doc
import com.demo.doccloud.domain.usecases.contracts.SaveDoc
import com.demo.doccloud.domain.usecases.contracts.SaveLocalDoc
import com.demo.doccloud.domain.usecases.contracts.ScheduleToSaveRemoteDoc
import javax.inject.Inject

class SaveDocImpl @Inject constructor(
    private val saveLocalDoc: SaveLocalDoc,
    private val scheduleToSaveRemoteDoc: ScheduleToSaveRemoteDoc
): SaveDoc {
    override suspend fun invoke(doc: Doc) {
        val rowNumber = saveLocalDoc(doc)
        scheduleToSaveRemoteDoc(rowNumber)
    }
}