package com.demo.doccloud.data.datasource.local.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.demo.doccloud.domain.Doc
import com.demo.doccloud.domain.DocStatus
import com.demo.doccloud.domain.Photo

@Entity(tableName = "doc_table")
data class DatabaseDoc constructor(
    val remoteId: Long,
    val name: String,
    val date: String,
    //photo's paths
    val pages: List<Photo>,
    val status: DocStatus,
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0,
)

fun List<DatabaseDoc>.asDomain(): List<Doc> {
    return map {
        Doc(
            remoteId = it.remoteId,
            name = it.name,
            date = it.date,
            pages = it.pages,
            status = it.status,
            localId = it.localId,
        )
    }
}

fun DatabaseDoc.asDomain(): Doc {
    return Doc(
        remoteId = this.remoteId,
        name = this.name,
        date = this.date,
        pages = this.pages,
        status = this.status,
        localId = this.localId,
    )
}
