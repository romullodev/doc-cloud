package com.demo.doccloud.data.datasource.local

import androidx.lifecycle.LiveData
import com.demo.doccloud.domain.Doc

interface LocalDataSource {
    suspend fun saveDocOnDevice(doc: Doc) : Long
    suspend fun deleteDocOnDevice(doc: Doc)
    suspend fun getDoc(id: Long): Doc
    suspend fun updateDoc(doc: Doc)
    fun getSavedDocs() : LiveData<List<Doc>>
}