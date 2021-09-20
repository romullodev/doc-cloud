package com.demo.doccloud.di

import com.demo.doccloud.FakeRepository
import com.demo.doccloud.data.repository.Repository
import com.demo.doccloud.data.repository.di.RepositoryModule
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton


@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class]
)
abstract class FakeRepositoryModule {
    @Singleton
    @Binds
    abstract fun bindFakeRepository(repository: FakeRepository): Repository
}