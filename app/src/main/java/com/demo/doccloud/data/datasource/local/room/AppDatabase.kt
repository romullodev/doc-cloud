package com.demo.doccloud.data.datasource.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.demo.doccloud.data.datasource.local.room.dao.DocDao
import com.demo.doccloud.data.datasource.local.room.entities.DatabaseDoc

@Database(
    entities =
    [DatabaseDoc::class],
    version = 1
)

@TypeConverters(AppConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val docDao: DocDao
}