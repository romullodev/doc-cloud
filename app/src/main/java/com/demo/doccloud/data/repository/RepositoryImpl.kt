package com.demo.doccloud.data.repository

import android.content.Intent
import com.demo.doccloud.data.datasource.DataSource
import com.demo.doccloud.domain.User
import com.demo.doccloud.utils.Result
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class RepositoryImpl @Inject constructor(
    private val remoteDatasource: DataSource
) : Repository {

    override suspend fun doLoginWithGoogle(data: Intent?) = remoteDatasource.doLoginWithGoogle(data)
    override suspend fun getUser() = remoteDatasource.getUser()
    override suspend fun doLogout() = remoteDatasource.doLogout()
}