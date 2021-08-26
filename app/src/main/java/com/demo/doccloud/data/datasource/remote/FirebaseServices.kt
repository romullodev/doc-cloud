package com.demo.doccloud.data.datasource.remote

import android.accounts.NetworkErrorException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.demo.doccloud.R
import com.demo.doccloud.di.IoDispatcher
import com.demo.doccloud.domain.Doc
import com.demo.doccloud.domain.User
import com.demo.doccloud.utils.AppConstants.Companion.DATABASE_DATE_KEY
import com.demo.doccloud.utils.AppConstants.Companion.DATABASE_DOCUMENTS_DIRECTORY
import com.demo.doccloud.utils.AppConstants.Companion.DATABASE_DOC_NAME_KEY
import com.demo.doccloud.utils.AppConstants.Companion.DATABASE_REMOTE_ID_KEY
import com.demo.doccloud.utils.AppConstants.Companion.DATABASE_TOTAL_PAGES_KEY
import com.demo.doccloud.utils.AppConstants.Companion.DATABASE_USERS_DIRECTORY
import com.demo.doccloud.utils.AppConstants.Companion.STORAGE_IMAGES_DIRECTORY
import com.demo.doccloud.utils.AppConstants.Companion.STORAGE_USERS_DIRECTORY
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
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class FirebaseServices @Inject constructor(
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage,
    private val database: FirebaseDatabase,
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

    //triggers from UploadDocWorker
    override suspend fun uploadDocFirebase(doc: Doc){
        withContext(dispatcher){
            val userId : String = auth.currentUser?.uid ?: return@withContext Result.error("Usuário não logado", null)
            //Firebase Storage
            val fireStorage = storage.reference
            //send all photos to cloud
            doc.pages.forEachIndexed { index, filePath ->
                //reference to save image into Storage
                val refStorageImage = fireStorage.child(
                    "$STORAGE_USERS_DIRECTORY/$userId/$STORAGE_IMAGES_DIRECTORY/${doc.remoteId}_$index.jpg"
                )
                //this code bellow can throw an exception
                //use a try catch block when invoke this function
                try {
                    val uriFile = Uri.fromFile(File(filePath))
                    val task1 = refStorageImage.putFile(uriFile)
                    task1.await()
                }catch (e: Exception){
                    Timber.d("Erro ao enviar imagens do documento para o servidor. \nDetalhes: $e")
                }
            }

            //Firebase Database
            val database = database.reference
            //reference to save values into database
            val refDatabase = database.child(DATABASE_USERS_DIRECTORY)
                .child(userId)
                .child(DATABASE_DOCUMENTS_DIRECTORY)
                .child(doc.remoteId.toString())

            //save into Real Database
            val mapDatabase: HashMap<String, String> = hashMapOf(
                DATABASE_DATE_KEY to doc.date,
                DATABASE_REMOTE_ID_KEY to doc.remoteId.toString(),
                DATABASE_DOC_NAME_KEY to doc.name,
                DATABASE_TOTAL_PAGES_KEY to doc.pages.size.toString(),
            )
            try {
                val task = refDatabase.setValue(mapDatabase)
                task.await()
            }catch (e: Exception){
                Timber.d("Erro ao enviar dados do documento para o servidor. \nDetalhes: $e")
            }
        }
    }

    override suspend fun deleteDocFirebase(remoteId: Long, pagesNumber: Int){
        withContext(dispatcher){
            val userId : String = auth.currentUser?.uid ?: return@withContext Result.error("Usuário não logado", null)
            //Firebase Storage
            val fireStorage = storage.reference
            //delete all photos from server
            for (index in 0 until pagesNumber){
                //reference to delete images from Storage
                val refStorageImage = fireStorage.child(
                    "$STORAGE_USERS_DIRECTORY/$userId/$STORAGE_IMAGES_DIRECTORY/${remoteId}_$index.jpg"
                )
                try {
                    val task1 = refStorageImage.delete()
                    task1.await()
                }catch (e: Exception){
                    Timber.d("Erro ao deletar a imagem ${remoteId}_$index.jpg do servidor. \nDetalhes: $e")
                }
            }
            //Firebase Database
            val database = database.reference
            //reference to delete values from database
            val refDatabase = database.child(DATABASE_USERS_DIRECTORY)
                .child(userId)
                .child(DATABASE_DOCUMENTS_DIRECTORY)
                .child(remoteId.toString())

            try {
                val task2 = refDatabase.removeValue()
                task2.await()
            }catch (e: Exception){
                Timber.d("Erro ao deletar dados do documento no servidor. \nDetalhes: $e")
            }
        }
    }
}