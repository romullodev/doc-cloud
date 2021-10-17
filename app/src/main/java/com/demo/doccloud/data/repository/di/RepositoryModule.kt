package com.demo.doccloud.data.repository.di

import com.demo.doccloud.data.repository.Repository
import com.demo.doccloud.data.repository.RepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Singleton
    @Binds
    abstract fun bindRepository(repository: RepositoryImpl): Repository
}