package com.demo.doccloud.data.datasource.local.persist

import android.content.Context
import com.demo.doccloud.R
import com.demo.doccloud.utils.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

class SharedPreferenceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PersistSimpleData{
    override suspend fun saveLong(key: String, value: Long): Result<Nothing?> {
        val sharePref = context.getSharedPreferences(
            context.getString(R.string.share_preference_doc_cloud_app_file_key),
            Context.MODE_PRIVATE
        ) ?: return Result.error("failure on get  sharedPreference instance")
        with(sharePref.edit()) {
            putLong(key, value)
            commit()
        }
        return Result.success(null)
    }

    override suspend fun getLong(key: String, defaultValue: Long): Result<Long>{
        val sharePref = context.getSharedPreferences(
            context.getString(R.string.share_preference_doc_cloud_app_file_key),
            Context.MODE_PRIVATE
        ) ?: return Result.error("failure on get  sharedPreference instance")

        val value = sharePref.getLong(key, defaultValue)
        return Result.success(value)
    }

    override suspend fun clearAllData() {
        val sharePref = context.getSharedPreferences(
            context.getString(R.string.share_preference_doc_cloud_app_file_key),
            Context.MODE_PRIVATE
        ) ?: return
        with(sharePref.edit()){
            clear()
            commit()
        }
    }
}