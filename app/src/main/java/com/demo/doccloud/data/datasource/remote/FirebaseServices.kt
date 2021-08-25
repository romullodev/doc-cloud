package com.demo.doccloud.data.datasource.remote

import android.accounts.NetworkErrorException
import android.content.Context
import android.content.Intent
import com.demo.doccloud.R
import com.demo.doccloud.di.IoDispatcher
import com.demo.doccloud.domain.User
import com.demo.doccloud.utils.Global
import com.demo.doccloud.utils.Result
import com.demo.doccloud.utils.asDomain
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject

class FirebaseServices @Inject constructor(
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context
) : RemoteDataSource {
    override suspend fun doLoginWithGoogle(data: Intent?): Result<User> {
        return withContext(dispatcher) {
            try {
                val signedInTask: Task<GoogleSignInAccount> =
                    GoogleSignIn.getSignedInAccountFromIntent(data)
                signedInTask.await()
                val account: GoogleSignInAccount = signedInTask.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                val signInCredentialTask = auth.signInWithCredential(credential)
                signInCredentialTask.await()
                return@withContext Result.success(auth.currentUser?.asDomain()!!)
            } catch (e: Exception) {
                if(e is ApiException){
                    return@withContext Result.error(context.getString(R.string.login_error_api_google))
                }
                if (e is NetworkErrorException || e is HttpException){
                    return@withContext Result.error(context.getString(R.string.common_no_internet))
                }
                return@withContext Result.error(context.getString(R.string.login_unknown_error))

            }
        }
    }

    override suspend fun getUser(): Result<User> {
        auth.currentUser?.let {
            val user = it.asDomain()
            Global.user = user
            return Result.success(user)
        }
        return Result.error(context.getString(R.string.login_user_not_logged_in))
    }

    override suspend fun doLogout(): Result<Boolean> {
        //this code bellow make user choose an account whenever he do log in after a logout
        return withContext(dispatcher) {
            auth.signOut()
            Global.user = null
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(context, gso)
            val taskGoogleClient = googleSignInClient.revokeAccess()
            try {
                taskGoogleClient.await()
                return@withContext Result.success(true)
            } catch (e: Exception) {
                return@withContext Result.error(context.getString(R.string.login_revokeAccess))
            }
        }
    }
}