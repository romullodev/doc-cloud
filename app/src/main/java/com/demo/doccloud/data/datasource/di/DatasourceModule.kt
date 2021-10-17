package com.demo.doccloud.data.datasource.di

import android.content.Context
import androidx.room.Room
import com.demo.doccloud.data.datasource.local.AppLocalServices
import com.demo.doccloud.data.datasource.local.LocalDataSource
import com.demo.doccloud.data.datasource.local.persist.PersistSimpleData
import com.demo.doccloud.data.datasource.local.persist.SharedPreferenceImpl
import com.demo.doccloud.data.datasource.local.room.AppDatabase
import com.demo.doccloud.data.datasource.remote.FirebaseServices
import com.demo.doccloud.data.datasource.remote.RemoteDataSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class DatasourceModule {

    @Binds
    abstract fun bindRemoteDataSource(remoteDatasource: FirebaseServices): RemoteDataSource

    @Binds
    abstract fun bindPersistSimpleData(persistSimpleData: SharedPreferenceImpl): PersistSimpleData

    @Binds
    abstract fun bindLocalDataSource(localDataSource: AppLocalServices): LocalDataSource
}

//provides outside implementations for this module
@Module
@InstallIn(SingletonComponent::class)
object HelperDatasourceModule {

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext app: Context): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "app_database"
        )
            .build()
    }

    @Provides
    fun providesFirebaseAuth() = FirebaseAuth.getInstance()

    @Provides
    fun providesFirebaseStorage() = FirebaseStorage.getInstance()

    @Provides
    fun providesFirebaseDatabase() = FirebaseDatabase.getInstance()
}