package com.demo.doccloud.data.datasource.remote

import android.accounts.NetworkErrorException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.demo.doccloud.R
import com.demo.doccloud.di.IoDispatcher
import com.demo.doccloud.domain.Doc
import com.demo.doccloud.domain.DocStatus
import com.demo.doccloud.domain.Photo
import com.demo.doccloud.domain.User
import com.demo.doccloud.utils.AppConstants.Companion.DATABASE_DATE_KEY
import com.demo.doccloud.utils.AppConstants.Companion.DATABASE_DOCUMENTS_DIRECTORY
import com.demo.doccloud.utils.AppConstants.Companion.DATABASE_DOC_NAME_KEY
import com.demo.doccloud.utils.AppConstants.Companion.DATABASE_JSON_PAGES_KEY
import com.demo.doccloud.utils.AppConstants.Companion.DATABASE_REMOTE_ID_KEY
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
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
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
                if (e is ApiException) {
                    return@withContext Result.error(context.getString(R.string.login_error_api_google))
                }
                if (e is NetworkErrorException || e is HttpException) {
                    return@withContext Result.error(context.getString(R.string.common_no_internet))
                }
                return@withContext Result.error(context.getString(R.string.login_unknown_error))

            }
        }
    }

    override suspend fun getUser(): Result<User> {
        return withContext(dispatcher) {
            auth.currentUser?.let {
                val user = it.asDomain()
                Global.user = user
                return@withContext Result.success(user)
            }
            return@withContext Result.error(context.getString(R.string.login_user_not_logged_in))
        }
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

    //trigger from UploadDocWorker
    override suspend fun uploadDocFirebase(doc: Doc) {
        withContext(dispatcher) {
            val userId: String =
                auth.currentUser?.uid ?: return@withContext Result.error("Usuário não logado", null)
            //Firebase Storage
            val fireStorage = storage.reference
            //send all photos to cloud
            doc.pages.forEach { photo ->
                //reference to save image into Storage
                val refStorageImage = fireStorage.child(
                    "$STORAGE_USERS_DIRECTORY/$userId/$STORAGE_IMAGES_DIRECTORY/${doc.remoteId}_${photo.id}.jpg"
                )
                //this code bellow can throw an exception
                //use a try catch block when invoke this function
                try {
                    val uriFile = Uri.fromFile(File(photo.path))
                    val task1 = refStorageImage.putFile(uriFile)
                    task1.await()
                } catch (e: Exception) {
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
                DATABASE_JSON_PAGES_KEY to Gson().toJson(
                    doc.pages.map {
                        it.id
                    }
                ),
            )
            try {
                val task = refDatabase.setValue(mapDatabase)
                task.await()
            } catch (e: Exception) {
                Timber.d("Erro ao enviar dados do documento para o servidor. \nDetalhes: $e")
            }
        }
    }

    //trigger from DeleteDocWorker
    override suspend fun deleteDocFirebase(remoteId: Long, pages: List<Photo>) {
        withContext(dispatcher) {
            val userId: String =
                auth.currentUser?.uid ?: return@withContext Result.error("Usuário não logado", null)
            //Firebase Storage
            val fireStorage = storage.reference
            //delete all photos from server
            pages.forEach {
                //reference to delete images from Storage
                val refStorageImage = fireStorage.child(
                    "$STORAGE_USERS_DIRECTORY/$userId/$STORAGE_IMAGES_DIRECTORY/${remoteId}_${it.id}.jpg"
                )
                try {
                    val task1 = refStorageImage.delete()
                    task1.await()
                } catch (e: Exception) {
                    Timber.d("Erro ao deletar a imagem ${remoteId}_${it.id}.jpg do servidor. \nDetalhes: $e")
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
            } catch (e: Exception) {
                Timber.d("Erro ao deletar dados do documento no servidor. \nDetalhes: $e")
            }
        }
    }

    //trigger from UpdateDocNameWorker
    override suspend fun updateDocNameFirebase(remoteId: Long, name: String): Result<Boolean> {
        return withContext(dispatcher) {
            val userId: String =
                auth.currentUser?.uid ?: return@withContext Result.error("Usuário não logado", null)
            //Firebase Database
            val database = database.reference
            //reference to save values into database
            val refDatabase =
                database.child("$DATABASE_USERS_DIRECTORY/$userId/$DATABASE_DOCUMENTS_DIRECTORY/$remoteId")
            //update doc name on Real Database
            val mapDatabase: HashMap<String, Any> = HashMap()
            mapDatabase[DATABASE_DOC_NAME_KEY] = name
            try {
                val task = refDatabase.updateChildren(mapDatabase)
                task.await()
            } catch (e: Exception) {
                return@withContext Result.error(
                    "Erro ao enviar dados do documento para o servidor. Detalhes: $e",
                    null
                )
            }
            return@withContext Result.success(true)
        }
    }

    //trigger from UpdateDocPageWorker
    override suspend fun updateDocPhotosFirebase(remoteId: Long, photo: Photo): Result<Boolean> {
        return withContext(dispatcher) {
            val userId: String =
                auth.currentUser?.uid ?: return@withContext Result.error("Usuário não logado", null)
            //Firebase Storage
            val fireStorage = storage.reference
            //delete old photo from server
            val index = photo.id
            val refStorageImage = fireStorage.child(
                "$STORAGE_USERS_DIRECTORY/$userId/$STORAGE_IMAGES_DIRECTORY/${remoteId}_${photo.id}.jpg"
            )
            try {
                val task1 = refStorageImage.delete()
                task1.await()
            } catch (e: Exception) {
                return@withContext Result.error(
                    "Erro ao deletar a imagem ${remoteId}_$index.jpg do servidor. \nDetalhes: $e",
                    null
                )
            }

            //upload new photo to server
            try {
                val uriFile = Uri.fromFile(File(photo.path))
                val task2 = refStorageImage.putFile(uriFile)
                task2.await()
            } catch (e: Exception) {
                return@withContext Result.error(
                    "Erro ao enviar imagens do documento para o servidor. \nDetalhes: $e",
                    null
                )
            }
            return@withContext Result.success(true)
        }
    }

    //trigger from DeleteDocPageWorker
    override suspend fun deleteDocPhotosFirebase(
        remoteId: Long,
        photo: Photo,
        jsonPages: String
    ): Result<Boolean> {
        return withContext(dispatcher) {
            val userId: String =
                auth.currentUser?.uid ?: return@withContext Result.error("Usuário não logado", null)
            //Firebase Storage
            val fireStorage = storage.reference
            //delete photo from server
            val index = photo.id
            val refStorageImage = fireStorage.child(
                "$STORAGE_USERS_DIRECTORY/$userId/$STORAGE_IMAGES_DIRECTORY/${remoteId}_${photo.id}.jpg"
            )
            try {
                val task = refStorageImage.delete()
                task.await()
            } catch (e: Exception) {
                return@withContext Result.error(
                    "Erro ao deletar a imagem ${remoteId}_${photo.id}.jpg do servidor. \nDetalhes: $e",
                    null
                )
            }

            //Firebase Database
            val database = database.reference
            //reference to save values into database
            val refDatabase =
                database.child("$DATABASE_USERS_DIRECTORY/$userId/$DATABASE_DOCUMENTS_DIRECTORY/$remoteId")
            //update doc pages on Real Database
            val mapDatabase: HashMap<String, Any> = HashMap()
            mapDatabase[DATABASE_JSON_PAGES_KEY] = jsonPages
            try {
                val task = refDatabase.updateChildren(mapDatabase)
                task.await()
            } catch (e: Exception) {
                return@withContext Result.error(
                    "Erro ao atualizar dados do documento no servidor. Detalhes: $e",
                    null
                )
            }
            return@withContext Result.success(true)
        }
    }

    //trigger from SyncDataWorker
    override suspend fun syncData(): Result<List<Doc>> {
        return withContext(dispatcher) {
            val userId: String =
                auth.currentUser?.uid ?: return@withContext Result.error("Usuário não logado", null)
            //Firebase Database
            val database = database.reference
            //reference to delete values from database
            val refDatabase =
                database.child("$DATABASE_USERS_DIRECTORY/$userId/$DATABASE_DOCUMENTS_DIRECTORY")

            //save the Firebase query task
            val source: TaskCompletionSource<Any> = TaskCompletionSource()

            refDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    source.setResult(p0)
                }

                override fun onCancelled(p0: DatabaseError) {
                    source.setResult(p0)
                }
            })
            //get the saved task
            val task = source.task
            //this code bellow can throw an exception
            task.await()
            val values = task.result as DataSnapshot
            val docs = ArrayList<Doc>()
            for (snapshot in values.children) {
                val remoteId = snapshot.child(DATABASE_REMOTE_ID_KEY).value.toString()
                val name = snapshot.child(DATABASE_DOC_NAME_KEY).value.toString()
                val date = snapshot.child(DATABASE_DATE_KEY).value.toString()
                val jsonPages = snapshot.child(DATABASE_JSON_PAGES_KEY).value.toString()
                val ids: List<Long> =
                    Gson().fromJson(jsonPages, Array<Long>::class.java).toList()
                val pages: List<Photo> = getPages(ids, userId, remoteId)
                docs.add(
                    Doc(
                        remoteId = remoteId.toLong(),
                        name = name,
                        date = date,
                        pages = pages,
                        status = DocStatus.SENT,
                    )
                )
            }
            return@withContext Result.success(docs)
        }
    }

    private suspend fun getPages(ids: List<Long>, userId: String, remoteId: String): List<Photo> {
        //Firebase Storage
        val fireStorage = storage.reference
        val pages = ArrayList<Photo>()
        ids.forEach { id->
            //reference to delete images from Storage
            val refStorageImage = fireStorage.child(
                "$STORAGE_USERS_DIRECTORY/$userId/$STORAGE_IMAGES_DIRECTORY/${remoteId}_${id}.jpg"
            )

            val tempLocalFile = File.createTempFile("images", "jpg")

            val task = refStorageImage.getFile(tempLocalFile)
            task.await()
            if(task.isSuccessful){
                //here, is all things Local temp file has been created
                val newFileOnFilesDir =
                    File(Global.getOutputDirectory(context), tempLocalFile.name)
                //copy file from cache Dir
                tempLocalFile.copyTo(newFileOnFilesDir, true)
                tempLocalFile.delete()
                val path = newFileOnFilesDir.path
                pages.add(
                    Photo(
                        id = id,
                        path = path
                    )
                )
            }else{
                Timber.d("failure on download file. \nDetails: ${task.exception}")
            }
        }
        return pages
    }
}