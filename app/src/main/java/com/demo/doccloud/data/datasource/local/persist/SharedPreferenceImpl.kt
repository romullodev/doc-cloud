package com.demo.doccloud.data.datasource.local.persist

import android.content.Context
import com.demo.doccloud.R
import com.demo.doccloud.di.IoDispatcher
import com.demo.doccloud.utils.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

class SharedPreferenceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : PersistSimpleData {
    override suspend fun saveLong(key: String, value: Long) {
        withContext(dispatcher) {
            val sharePref = context.getSharedPreferences(
                context.getString(R.string.share_preference_doc_cloud_app_file_key),
                Context.MODE_PRIVATE
            ) ?: throw Exception(context.getString(R.string.share_preference_instance_error))
            with(sharePref.edit()) {
                putLong(key, value)
                commit()
            }
        }
    }

    override suspend fun getLong(key: String, defaultValue: Long): Long {
        return withContext(dispatcher) {
            val sharePref = context.getSharedPreferences(
                context.getString(R.string.share_preference_doc_cloud_app_file_key),
                Context.MODE_PRIVATE
            ) ?: throw Exception(context.getString(R.string.share_preference_instance_error))

            return@withContext sharePref.getLong(key, defaultValue)
        }
    }

    override suspend fun clearAllData() {
        withContext(dispatcher) {
            val sharePref = context.getSharedPreferences(
                context.getString(R.string.share_preference_doc_cloud_app_file_key),
                Context.MODE_PRIVATE
            ) ?: throw Exception(context.getString(R.string.share_preference_instance_error))
            with(sharePref.edit()) {
                clear()
                commit()
            }
        }
    }
}