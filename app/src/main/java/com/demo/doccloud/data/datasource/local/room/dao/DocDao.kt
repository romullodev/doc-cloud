package com.demo.doccloud.data.datasource.local.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.demo.doccloud.data.datasource.local.room.entities.DatabaseDoc

@Dao
interface DocDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(list: List<DatabaseDoc>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(doc: DatabaseDoc) : Long

    @Query("SELECT * FROM doc_table WHERE localId = :id")
    fun getDoc(id: Long): DatabaseDoc

    @Query("UPDATE doc_table SET name = :name WHERE localId = :id")
    fun updateDocName(id: Long, name: String)

    @Update
    fun update(databaseDoc: DatabaseDoc)

    @Delete
    fun delete(doc: DatabaseDoc)

    @Query("delete from doc_table")
    fun clearTable()

    @Query("select * from doc_table")
    fun getDocs(): LiveData<List<DatabaseDoc>>
}