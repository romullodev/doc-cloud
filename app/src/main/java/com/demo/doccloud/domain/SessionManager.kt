package com.demo.doccloud.domain

import android.content.Context
import android.content.SharedPreferences
import com.demo.doccloud.R
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//Session manager to save and fetch data from SharedPreferences
//class SessionManager @Inject constructor(
//   @ApplicationContext private val context: Context
//) {
class SessionManager(
    val context: Context,
    private val ioDispatcher: CoroutineDispatcher
) {
    private var prefs: SharedPreferences =
        context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)

    companion object {
        const val USER_TOKEN = "user_token"
    }

    //Function to save auth token
    suspend fun saveAuthToken(token: String) {
        withContext(ioDispatcher) {
            val editor = prefs.edit()
            editor.putString(USER_TOKEN, token)
            editor.apply()
        }
    }

    //Function to fetch auth token
    suspend fun fetchAuthToken(): String? {
        return withContext(Dispatchers.IO) {
            return@withContext prefs.getString(USER_TOKEN, null)
        }
    }

    suspend fun clearAuthToken(): Boolean {
        return withContext(ioDispatcher) {
            with(prefs.edit()) {
                remove(USER_TOKEN)
                commit()
            }
            return@withContext true
        }
    }
}