package com.demo.doccloud.data.datasource.local.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.demo.doccloud.domain.Doc

@Entity(tableName = "doc_table")
data class DatabaseDoc constructor(
    val name: String,
    val date: String,
    //photo's paths
    val pages: List<String>,
    val status: String,
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
)

fun List<DatabaseDoc>.asDomain(): List<Doc> {
    return map {
        Doc(
            name = it.name,
            date = it.date,
            pages = it.pages,
            status = it.status,
            id = it.id,
        )
    }
}
