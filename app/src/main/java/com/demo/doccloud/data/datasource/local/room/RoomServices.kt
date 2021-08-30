package com.demo.doccloud.data.datasource.local.room

import androidx.lifecycle.map
import com.demo.doccloud.data.datasource.local.LocalDataSource
import com.demo.doccloud.data.datasource.local.room.entities.DatabaseDoc
import com.demo.doccloud.data.datasource.local.room.entities.asDomain
import com.demo.doccloud.di.IoDispatcher
import com.demo.doccloud.domain.Doc
import com.demo.doccloud.domain.Photo
import com.demo.doccloud.domain.asDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class RoomServices @Inject constructor(
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
    private val appDatabase: AppDatabase
) : LocalDataSource {
    override suspend fun saveDocOnDevice(doc: Doc) = withContext(dispatcher) {
        return@withContext appDatabase.docDao.insert(doc.asDatabase())
    }

    override suspend fun deleteDocOnDevice(doc: Doc) = withContext(dispatcher) {
        appDatabase.docDao.delete(doc.asDatabase(copyIdFlag = true))
        doc.pages.forEach {
            File(it.path).delete()
        }
    }

    override suspend fun getDoc(id: Long) = withContext(dispatcher) {
        appDatabase.docDao.getDoc(id).asDomain()
    }

    override suspend fun updateDoc(doc: Doc) = withContext(dispatcher) {
        appDatabase.docDao.update(doc.asDatabase(copyIdFlag = true))
    }

    override fun getSavedDocs() = appDatabase.docDao.getDocs().map {
        it.asDomain()
    }

    override suspend fun updateDocName(id: Long, name: String) = withContext(dispatcher) {
        appDatabase.docDao.updateDocName(id, name)
    }


    override suspend fun updateDocPhoto(localId: Long, photo: Photo) = withContext(dispatcher) {
        val doc: Doc = appDatabase.docDao.getDoc(localId).asDomain()
        val pages = doc.pages.map {
            if(it.id == photo.id) photo else it
        }
        val databaseDoc: DatabaseDoc =
            doc.copy(pages = pages).asDatabase(copyIdFlag = true)
        appDatabase.docDao.update(databaseDoc)
    }

    override suspend fun deleteDocPhoto(localId: Long, photo: Photo) = withContext(dispatcher) {
        val doc: Doc = appDatabase.docDao.getDoc(localId).asDomain()
        val newPages = doc.pages.filter{ photoFromDoc: Photo ->
            photoFromDoc.id != photo.id
        }
        val databaseDoc: DatabaseDoc =
            doc.copy(pages = newPages).asDatabase(copyIdFlag = true)
        appDatabase.docDao.update(databaseDoc)
    }

}