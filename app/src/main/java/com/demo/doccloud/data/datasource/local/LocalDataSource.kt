package com.demo.doccloud.data.datasource.local

import androidx.lifecycle.LiveData
import com.demo.doccloud.domain.Doc

interface LocalDataSource {
    suspend fun saveDocOnDevice(doc: Doc)
    suspend fun deleteDocOnDevice(doc: Doc)
    fun getSavedDocs() : LiveData<List<Doc>>
}