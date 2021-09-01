package com.demo.doccloud.di

import com.demo.doccloud.data.datasource.local.LocalDataSource
import com.demo.doccloud.data.datasource.local.AppLocalServices
import com.demo.doccloud.data.datasource.local.persist.PersistSimpleData
import com.demo.doccloud.data.datasource.local.persist.SharedPreferenceImpl
import com.demo.doccloud.data.datasource.remote.RemoteDataSource
import com.demo.doccloud.data.datasource.remote.FirebaseServices
import com.demo.doccloud.data.repository.Repository
import com.demo.doccloud.data.repository.RepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BindsModule {

    @Singleton
    @Binds
    abstract fun bindRepository(repository: RepositoryImpl): Repository

    @Binds
    abstract fun bindRemoteDataSource(remoteDatasource: FirebaseServices): RemoteDataSource

    @Binds
    abstract fun bindPersistSimpleData(persistSimpleData: SharedPreferenceImpl): PersistSimpleData

    @Binds
    abstract fun bindLocalDataSource(localDataSource: AppLocalServices): LocalDataSource
}