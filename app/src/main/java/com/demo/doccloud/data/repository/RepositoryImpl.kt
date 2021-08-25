package com.demo.doccloud.data.repository

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import com.demo.doccloud.R
import com.demo.doccloud.data.datasource.local.LocalDataSource
import com.demo.doccloud.data.datasource.local.room.entities.DatabaseDoc
import com.demo.doccloud.data.datasource.remote.RemoteDataSource
import com.demo.doccloud.domain.Doc
import com.demo.doccloud.utils.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class RepositoryImpl @Inject constructor(
    private val remoteDatasource: RemoteDataSource,
    private val localDatasource: LocalDataSource,
    @ApplicationContext private val context: Context,
) : Repository {
    override val docs: LiveData<List<Doc>> get() = localDatasource.getSavedDocs()

    override suspend fun doLoginWithGoogle(data: Intent?) = remoteDatasource.doLoginWithGoogle(data)
    override suspend fun getUser() = remoteDatasource.getUser()
    override suspend fun doLogout() = remoteDatasource.doLogout()
    override suspend fun saveDoc(doc: Doc) : Result<Boolean> {
        localDatasource.saveDocOnDevice(doc)
        //schedule to send docs to server
        //code here
        return Result.success(true)
    }

    override suspend fun deleteDoc(doc: Doc): Result<String> {
        localDatasource.deleteDocOnDevice(doc)
        //schedule to delete docs from server
        //code here
        return Result.success(context.getString(R.string.home_toast_delete_success, doc.name))
    }
}