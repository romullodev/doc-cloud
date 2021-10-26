package com.demo.doccloud.data.datasource.local.persist

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.*
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.asLiveData
import com.demo.doccloud.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "docCloudDataStore")

class DataStoreManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : PersistSimpleData {

    override suspend fun saveLong(key: String, value: Long) {
        context.dataStore.edit { preferences ->
            preferences[longPreferencesKey(key)] = value
        }
    }

    override suspend fun getLong(key: String, defaultValue: Long): Long {
        val preferencesKey = longPreferencesKey(key)
        val preferences = context.dataStore.data.first()
        return preferences[preferencesKey] ?: defaultValue

//        return withContext(dispatcher){
//            return@withContext context.dataStore.data
//                .map { preferences ->
//                    preferences[longPreferencesKey(key)] ?: defaultValue
//                }.asLiveData()
//        }
    }

    override suspend fun clearAllData() {
        context.dataStore.edit {
            it.clear()
        }
    }
}