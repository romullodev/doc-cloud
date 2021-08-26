package com.demo.doccloud.data.datasource.local.room

import androidx.lifecycle.map
import com.demo.doccloud.data.datasource.local.LocalDataSource
import com.demo.doccloud.data.datasource.local.room.entities.asDomain
import com.demo.doccloud.di.IoDispatcher
import com.demo.doccloud.domain.Doc
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
            File(it).delete()
        }
    }

    override suspend fun getDoc(id: Long): Doc {
        return appDatabase.docDao.getDoc(id.toInt()).asDomain()
    }

    override suspend fun updateDoc(doc: Doc) {
        appDatabase.docDao.update(doc.asDatabase(copyIdFlag = true))
    }

    override fun getSavedDocs() = appDatabase.docDao.getDocs().map {
        it.asDomain()
    }
}