package com.demo.doccloud.domain

import com.demo.doccloud.data.datasource.local.room.entities.DatabaseDoc

data class Doc(
    val remoteId: Long,
    val name: String,
    val date: String,
    //photo's paths
    val pages: List<String>,
    val status: DocStatus,
    val localId: Long = 0,
)

fun Doc.asDatabase(copyIdFlag: Boolean = false): DatabaseDoc {
    return if(copyIdFlag){
        DatabaseDoc(
            remoteId = this.remoteId,
            name = this.name,
            date = this.date,
            pages = this.pages,
            status = this.status,
            localId = this.localId
        )
    }else{
        DatabaseDoc(
            remoteId = this.remoteId,
            name = this.name,
            date = this.date,
            pages = this.pages,
            status = this.status
        )
    }
}

enum class DocStatus{
    SENT, SENDING, NOT_SENT
}