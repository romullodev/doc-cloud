package com.demo.doccloud.domain

data class SyncStrategy (
    //expiration field  is localed on ../user_database directory on firebase
    val expiration: Long,
    //lastUpdated and userId fields are localed on ../user_database/uid directory on firebase
    val lastUpdated: Long,
    //customId is create using local timestamp
    val customId: Long ,
)