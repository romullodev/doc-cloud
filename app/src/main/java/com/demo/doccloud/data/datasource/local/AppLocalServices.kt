package com.demo.doccloud.data.datasource.local

import androidx.lifecycle.map
import com.demo.doccloud.data.datasource.local.persist.PersistSimpleData
import com.demo.doccloud.data.datasource.local.room.AppDatabase
import com.demo.doccloud.data.datasource.local.room.entities.DatabaseDoc
import com.demo.doccloud.data.datasource.local.room.entities.asDomain
import com.demo.doccloud.di.IoDispatcher
import com.demo.doccloud.domain.entities.Doc
import com.demo.doccloud.domain.entities.DocStatus
import com.demo.doccloud.domain.entities.Photo
import com.demo.doccloud.domain.entities.asDatabase
import com.demo.doccloud.utils.AppConstants.Companion.DATABASE_DEFAULT_CUSTOM_ID
import com.demo.doccloud.utils.AppConstants.Companion.LOCAL_DATABASE_CUSTOM_ID_KEY
import com.demo.doccloud.utils.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class AppLocalServices @Inject constructor(
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
    private val appDatabase: AppDatabase,
    private val persistSimpleData: PersistSimpleData,
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
        val doc = appDatabase.docDao.getDoc(id)
        appDatabase.docDao.update(doc.copy(name = name, status = DocStatus.NOT_SENT))
    }


    override suspend fun updateDocPhoto(localId: Long, photo: Photo) = withContext(dispatcher) {
        val doc: Doc = appDatabase.docDao.getDoc(localId).asDomain()
        val pages = doc.pages.map {
            if (it.id == photo.id) photo else it
        }
        val databaseDoc: DatabaseDoc =
            doc.copy(pages = pages, status = DocStatus.NOT_SENT).asDatabase(copyIdFlag = true)
        appDatabase.docDao.update(databaseDoc)
    }

    override suspend fun deleteDocPhoto(localId: Long, photo: Photo) = withContext(dispatcher) {
        val doc: Doc = appDatabase.docDao.getDoc(localId).asDomain()
        val newPages = doc.pages.filter { photoFromDoc: Photo ->
            photoFromDoc.id != photo.id
        }
        val databaseDoc: DatabaseDoc =
            doc.copy(pages = newPages, status = DocStatus.NOT_SENT).asDatabase(copyIdFlag = true)
        appDatabase.docDao.update(databaseDoc)
    }

    override suspend fun syncData(docs: List<Doc>) = withContext(dispatcher) {
        appDatabase.docDao.clearTable()
        appDatabase.docDao.insertAll(docs.asDatabase())
    }

//    override suspend fun getSavedCustomId(): Long {
//        return withContext(dispatcher) {
//            val result =  persistSimpleData.getLong(
//                LOCAL_DATABASE_CUSTOM_ID_KEY,
//                DATABASE_DEFAULT_CUSTOM_ID
//            )
//            when(result.status){
//                Result.Status.SUCCESS -> {
//                    return@withContext result.data!!
//                }
//                Result.Status.ERROR -> {
//                    return@withContext DATABASE_DEFAULT_CUSTOM_ID
//                }
//            }
//        }
//    }
//
//    override suspend fun saveCustomId(): Result<Long> {
//        return withContext(dispatcher) {
//            val id = System.currentTimeMillis()
//            val result = persistSimpleData.saveLong(LOCAL_DATABASE_CUSTOM_ID_KEY, id)
//            when(result.status){
//                Result.Status.SUCCESS -> {
//                    return@withContext Result.success(id)
//                }
//                Result.Status.ERROR -> {
//                    return@withContext Result.error("failure on save custom id", null)
//                }
//            }
//        }
//    }

    override suspend fun addPhotosToDoc(localId: Long, photos: List<Photo>) {
        withContext(dispatcher){
            val doc : DatabaseDoc = appDatabase.docDao.getDoc(localId)
            val addedPages = ArrayList(doc.pages)
            addedPages.addAll(photos)
            appDatabase.docDao.update(
                doc.copy(pages = addedPages)
            )
        }
    }

    override suspend fun clearAllData() {
        withContext(dispatcher){
            appDatabase.clearAllTables()
            persistSimpleData.clearAllData()
        }
    }

    override suspend fun saveLong(key: String, value: Long) {
        withContext(dispatcher){
            persistSimpleData.saveLong(key, value)
        }
    }

    override suspend fun getLong(key: String, defaultValue: Long) = withContext(dispatcher){
        persistSimpleData.getLong(key, defaultValue)
    }

    override suspend fun insertDocs(docs: List<Doc>)  = withContext(dispatcher){
        appDatabase.docDao.insertAll(docs.asDatabase())
    }

    override suspend fun clearDocs() = withContext(dispatcher) {
        appDatabase.docDao.clearTable()
    }
}