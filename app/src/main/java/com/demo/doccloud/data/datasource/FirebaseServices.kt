package com.demo.doccloud.data.datasource

import android.content.Context
import android.content.Intent
import com.demo.doccloud.R
import com.demo.doccloud.di.IoDispatcher
import com.demo.doccloud.domain.User
import com.demo.doccloud.utils.GlobalUtil
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
import javax.inject.Inject

class FirebaseServices @Inject constructor(
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context
) : DataSource {
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
            } catch (e: ApiException) {
                return@withContext Result.error(e)
            }
        }
    }

    override suspend fun getUser(): Result<User> {
        auth.currentUser?.let {
            val user = it.asDomain()
            GlobalUtil.user = user
            return Result.success(user)
        }
        return Result.error(Exception("Usuário não encontrado"))
    }

    override suspend fun doLogout(): Result<Boolean> {
        //this code bellow make user choose an account whenever he do log in after a logout
        return withContext(dispatcher) {
            auth.signOut()
            GlobalUtil.user = null
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
                return@withContext Result.error(e)
            }
        }
    }
}