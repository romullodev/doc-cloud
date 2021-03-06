package com.demo.doccloud.data.datasource.local.persist

import com.demo.doccloud.utils.Result
interface PersistSimpleData {
    suspend fun saveLong(key: String, value: Long)
    suspend fun getLong(key: String, defaultValue: Long): Long
    suspend fun clearAllData()
}