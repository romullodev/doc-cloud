package com.demo.doccloud.data.datasource.local

import androidx.lifecycle.LiveData
import com.demo.doccloud.domain.Doc
import com.demo.doccloud.domain.Photo

interface LocalDataSource {
    suspend fun saveDocOnDevice(doc: Doc) : Long
    suspend fun deleteDocOnDevice(doc: Doc)
    suspend fun getDoc(id: Long): Doc
    suspend fun updateDoc(doc: Doc)
    fun getSavedDocs() : LiveData<List<Doc>>
    suspend fun updateDocName(id: Long, name: String)
    suspend fun updateDocPhoto(localId: Long, photo: Photo)
    suspend fun deleteDocPhoto(localId: Long, photo: Photo)
}