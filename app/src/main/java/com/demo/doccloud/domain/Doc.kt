package com.demo.doccloud.domain

import com.demo.doccloud.data.datasource.local.room.entities.DatabaseDoc

data class Doc(
    val name: String,
    val date: String,
    //photo's paths
    val pages: List<String>,
    val status: String,
    val id: Long = 0,
)

fun Doc.asDatabase(copyIdFlag: Boolean = false): DatabaseDoc {
    return if(copyIdFlag){
        DatabaseDoc(
            name = this.name,
            date = this.date,
            pages = this.pages,
            status = this.status,
            id = this.id
        )
    }else{
        DatabaseDoc(
            name = this.name,
            date = this.date,
            pages = this.pages,
            status = this.status
        )
    }
}