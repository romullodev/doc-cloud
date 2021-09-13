package com.demo.doccloud.data.datasource.local

import androidx.lifecycle.LiveData
import com.demo.doccloud.domain.entities.Doc
import com.demo.doccloud.domain.entities.Photo
import com.demo.doccloud.utils.Result

interface LocalDataSource {
    suspend fun saveDocOnDevice(doc: Doc) : Long
    suspend fun deleteDocOnDevice(doc: Doc)
    suspend fun getDoc(id: Long): Doc
    suspend fun updateDoc(doc: Doc)
    fun getSavedDocs() : LiveData<List<Doc>>
    suspend fun updateDocName(id: Long, name: String)
    suspend fun updateDocPhoto(localId: Long, photo: Photo)
    suspend fun deleteDocPhoto(localId: Long, photo: Photo)
    suspend fun syncData(docs: List<Doc>)
    suspend fun addPhotosToDoc(localId: Long, photos: List<Photo>)
    suspend fun clearAllData() //clear database and persist dada
    suspend fun saveLong(key: String, value: Long)
    suspend fun getLong(key: String, defaultValue: Long) : Long
    suspend fun insertDocs(docs: List<Doc>)
    suspend fun clearDocs()
}