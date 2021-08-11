package com.demo.doccloud.di

import com.demo.doccloud.data.datasource.DataSource
import com.demo.doccloud.data.datasource.FirebaseServices
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
    abstract fun bindDataSource(datasource: FirebaseServices): DataSource
}