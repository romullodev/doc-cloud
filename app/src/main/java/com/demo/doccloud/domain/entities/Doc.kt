package com.demo.doccloud.domain.entities

import com.demo.doccloud.data.datasource.local.room.entities.DatabaseDoc

data class Doc(
    val remoteId: Long,
    val name: String,
    val date: String,
    //photo's paths
    val pages: List<Photo>,
    val status: DocStatus,
    val localId: Long = 0,
)

//transform without copy localId since it'll be generate automatically from database
fun List<Doc>.asDatabase(): List<DatabaseDoc> {
    return map {
        DatabaseDoc(
            remoteId = it.remoteId,
            name = it.name,
            date = it.date,
            pages = it.pages,
            status = it.status,
        )
    }
}

fun Doc.asDatabase(copyIdFlag: Boolean = false): DatabaseDoc {
    return if (copyIdFlag) {
        DatabaseDoc(
            remoteId = this.remoteId,
            name = this.name,
            date = this.date,
            pages = this.pages,
            status = this.status,
            localId = this.localId
        )
    } else {
        DatabaseDoc(
            remoteId = this.remoteId,
            name = this.name,
            date = this.date,
            pages = this.pages,
            status = this.status
        )
    }
}

enum class DocStatus {
    SENT, SENDING, NOT_SENT
}