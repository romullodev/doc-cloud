package com.demo.doccloud.domain.usecases.impl

import android.content.Context
import com.demo.doccloud.R
import com.demo.doccloud.domain.entities.Doc
import com.demo.doccloud.domain.usecases.contracts.DeleteDoc
import com.demo.doccloud.domain.usecases.contracts.DeleteLocalDoc
import com.demo.doccloud.domain.usecases.contracts.ScheduleToDeleteRemoteDoc
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DeleteDocImpl @Inject constructor(
    private val deleteLocalDoc: DeleteLocalDoc,
    private val scheduleToDeleteRemoteDoc: ScheduleToDeleteRemoteDoc,
    @ApplicationContext private val context: Context
): DeleteDoc {
    override suspend fun invoke(doc: Doc): String {
        deleteLocalDoc(doc)
        scheduleToDeleteRemoteDoc(doc.remoteId, Gson().toJson(doc.pages))
        return context.getString(R.string.home_toast_delete_success, doc.name)
    }
}