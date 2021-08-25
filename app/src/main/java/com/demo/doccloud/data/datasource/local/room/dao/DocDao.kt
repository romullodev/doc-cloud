package com.demo.doccloud.data.datasource.local.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.demo.doccloud.data.datasource.local.room.entities.DatabaseDoc

@Dao
interface DocDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(doc: DatabaseDoc)

    @Delete
    fun delete(doc: DatabaseDoc)

    @Query("delete from doc_table")
    fun clearTable()

    @Query("select * from doc_table")
    fun getDocs(): LiveData<List<DatabaseDoc>>
}