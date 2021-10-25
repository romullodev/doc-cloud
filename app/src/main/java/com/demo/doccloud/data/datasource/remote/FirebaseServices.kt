package com.demo.doccloud.data.datasource.remote

import android.accounts.NetworkErrorException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.demo.doccloud.R
import com.demo.doccloud.di.IoDispatcher
import com.demo.doccloud.domain.*
import com.demo.doccloud.domain.entities.*
import com.demo.doccloud.utils.AppConstants.Companion.DATABASE_APP_LEVEL_EXCLUDE_TEMP_TIME_KEY
import com.demo.doccloud.utils.AppConstants.Companion.DATABASE_APP_LEVEL_EXPIRATION_KEY
import com.demo.doccloud.utils.AppConstants.Companion.DATABASE_APP_LEVEL_STRATEGY_KEY
import com.demo.doccloud.utils.AppConstants.Companion.DATABASE_DATE_KEY
import com.demo.doccloud.utils.AppConstants.Companion.DATABASE_DOCUMENTS_DIRECTORY
import com.demo.doccloud.utils.AppConstants.Companion.DATABASE_DOC_NAME_KEY
import com.demo.doccloud.utils.AppConstants.Companion.DATABASE_JSON_PAGES_KEY
import com.demo.doccloud.utils.AppConstants.Companion.DATABASE_LAST_UPDATED_KEY
import com.demo.doccloud.utils.AppConstants.Companion.DATABASE_REMOTE_ID_KEY
import com.demo.doccloud.utils.AppConstants.Companion.DATABASE_SYNC_STRATEGY_KEY
import com.demo.doccloud.utils.AppConstants.Companion.DATABASE_USERS_DIRECTORY
import com.demo.doccloud.utils.AppConstants.Companion.REMOTE_DATABASE_CUSTOM_ID_KEY
import com.demo.doccloud.utils.AppConstants.Companion.STORAGE_IMAGES_DIRECTORY
import com.demo.doccloud.utils.AppConstants.Companion.STORAGE_TEMP_DIRECTORY
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
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
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

    override suspend fun doLoginWithGoogle(data: Intent?): User {
        return withContext(dispatcher) {
            try {
                val signedInTask: Task<GoogleSignInAccount> =
                    GoogleSignIn.getSignedInAccountFromIntent(data)
                signedInTask.await()
                val account: GoogleSignInAccount = signedInTask.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                val signInCredentialTask = auth.signInWithCredential(credential)
                signInCredentialTask.await()
                return@withContext auth.currentUser?.asDomain()!!
            } catch (e: Exception) {
                if (e is ApiException) {
                    throw Exception(context.getString(R.string.login_error_api_google))
                }
                if (e is NetworkErrorException || e is HttpException) {
                    throw Exception(context.getString(R.string.common_no_internet))
                }
                Timber.d(e.toString())
                throw Exception(context.getString(R.string.common_unknown_error))
            }
        }
    }

    override suspend fun doLoginByEmail(email: String, password: String) = withContext(dispatcher) {
        try {
            val taskLogin = FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            taskLogin.await()
        } catch (e: Exception) {
            throw Exception(context.getString(R.string.login_error_sign_in_with_email))
        }
        val firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser ?: throw Exception(
            context.getString(R.string.login_error_retrieve_logged_user)
        )

        return@withContext firebaseUser.asDomain()
    }

    override suspend fun registerUser(params: SignUpParams) =
        withContext(dispatcher) {
            try {
                val authTask =
                    auth.createUserWithEmailAndPassword(params.email, params.password)
                authTask.await()
            } catch (e: Exception) {
                throw Exception(context.getString(R.string.signup_error_on_register_user))
            }

            val firebaseUser: FirebaseUser = auth.currentUser
                ?: throw Exception(context.getString(R.string.signup_error_user_not_found))

            try {
                val profileUpdates =
                    UserProfileChangeRequest.Builder().setDisplayName(params.name).build()
                val updateProfileTask = firebaseUser.updateProfile(profileUpdates)
                updateProfileTask.await()
            } catch (e: Exception) {
                throw Exception(context.getString(R.string.signup_error_update_user_profile))
            }
            return@withContext firebaseUser.asDomain()
            //STOP
//            //send customId to database
//            val userId: String = firebaseUser.uid
//            //Firebase Database
//            val database = database.reference
//            //reference to save values into database
//            val refDatabase =
//                database.child("$DATABASE_USERS_DIRECTORY/$userId/$DATABASE_SYNC_STRATEGY_KEY")
//
//            //save into Real Database
//            //set lastUpdated (field from Sync Strategy Model) to 0 (TIMESTAMP) (See SyncDataWorker)
//            //set customId
//
//            val mapDatabase: HashMap<String, String> = hashMapOf(
//                REMOTE_DATABASE_CUSTOM_ID_KEY to customId.toString(),
//                DATABASE_LAST_UPDATED_KEY to 0L.toString()
//            )
//            try {
//                val sendValuesTask = refDatabase.setValue(mapDatabase)
//                sendValuesTask.await()
//                return@withContext firebaseUser.asDomain()
//            }catch (e: Exception){
//                throw Exception(context.getString(R.string.signup_error_send_custom_id_to_server))
//            }
        }

    override suspend fun recoverPassword(email: String) {
        withContext(dispatcher) {
            val taskSend = auth.sendPasswordResetEmail(email)
            taskSend.await()
        }
    }

    override suspend fun getUser(): User {
        return withContext(dispatcher) {
            auth.currentUser?.let {
                return@withContext it.asDomain()
            }
            throw Exception(
                context.getString(R.string.common_user_not_logged_in)
            )
        }
    }

    override suspend fun doLogout() {
        //this code bellow make user choose an account whenever he do log in after a logout
        return withContext(dispatcher) {
            auth.signOut()
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(context, gso)
            val taskGoogleClient = googleSignInClient.revokeAccess()
            try {
                taskGoogleClient.await()
                return@withContext
            } catch (e: Exception) {
                return@withContext
            }
        }
    }

    //trigger from UploadDocWorker
    override suspend fun uploadDocFirebase(doc: Doc) {
        withContext(dispatcher) {
            val userId: String =
                auth.currentUser?.uid
                    ?: throw Exception(context.getString(R.string.common_user_not_logged_in))
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
                    throw e
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
                throw e
            }
        }
    }

    //trigger from DeleteDocWorker
    override suspend fun deleteDocFirebase(remoteId: Long, pages: List<Photo>) {
        withContext(dispatcher) {
            val userId: String =
                auth.currentUser?.uid
                    ?: throw Exception(context.getString(R.string.common_user_not_logged_in))
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
                    throw e
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
                throw e
            }
        }
    }

    //trigger from UpdateDocNameWorker
    override suspend fun updateDocNameFirebase(remoteId: Long, name: String) {
        withContext(dispatcher) {
            val userId: String =
                auth.currentUser?.uid
                    ?: throw Exception(context.getString(R.string.common_user_not_logged_in))
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
                throw e
            }
        }
    }

    //trigger from UpdateDocPageWorker
    override suspend fun updateDocPhotoFirebase(remoteId: Long, photo: Photo) {
        withContext(dispatcher) {
            val userId: String =
                auth.currentUser?.uid
                    ?: throw Exception(context.getString(R.string.common_user_not_logged_in))
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
                throw e
            }

            //upload new photo to server
            try {
                val uriFile = Uri.fromFile(File(photo.path))
                val task2 = refStorageImage.putFile(uriFile)
                task2.await()
            } catch (e: Exception) {
                throw e
            }
        }
    }

    override suspend fun deleteDocPhotosFirebase(
        remoteId: Long,
        photo: Photo,
        jsonPages: String
    ) {
        withContext(dispatcher) {
            val userId: String =
                auth.currentUser?.uid
                    ?: throw Exception(context.getString(R.string.common_user_not_logged_in))
            //Firebase Storage
            val fireStorage = storage.reference
            //delete photo from server
            val refStorageImage = fireStorage.child(
                "$STORAGE_USERS_DIRECTORY/$userId/$STORAGE_IMAGES_DIRECTORY/${remoteId}_${photo.id}.jpg"
            )
            try {
                val task = refStorageImage.delete()
                task.await()
            } catch (e: Exception) {
                //could customize this exception
                throw e
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
                //could customize this exception
                throw e
            }
        }
    }

    //trigger from SyncDataWorker
    override suspend fun syncData(customId: Long): List<Doc> {
        return withContext(dispatcher) {
            val userId: String =
                auth.currentUser?.uid
                    ?: throw Exception(context.getString(R.string.common_user_not_logged_in))
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

            //update sync strategy info on firebase
            //reference to save values into database
            val refSyncStrategy =
                database.child("$DATABASE_USERS_DIRECTORY/$userId/$DATABASE_SYNC_STRATEGY_KEY")
            //save into Real Database
            //set lastUpdated (field from Sync Strategy Model) to NOW (TIMESTAMP) (See SyncDataWorker)
            val mapDatabase: HashMap<String, Any> = HashMap()
            mapDatabase[DATABASE_LAST_UPDATED_KEY] = System.currentTimeMillis().toString()
            //update customId on firebase of THIS device
            mapDatabase[REMOTE_DATABASE_CUSTOM_ID_KEY] = customId
            val sendValuesTask = refSyncStrategy.updateChildren(mapDatabase)
            sendValuesTask.await()

            return@withContext docs
        }
    }

    override suspend fun getSyncStrategy(): SyncStrategy {
        return withContext(dispatcher) {
            val userId: String =
                auth.currentUser?.uid
                    ?: throw Exception(context.getString(R.string.common_user_not_logged_in))
            //Firebase Database
            val database = database.reference
            //reference to get expiration info
            val refDbExpiration = database.child(DATABASE_USERS_DIRECTORY)
            //reference to get sync strategy info directory (users level)
            val refDbSyncStrategy =
                database.child("$DATABASE_USERS_DIRECTORY/$userId/$DATABASE_SYNC_STRATEGY_KEY")

            //to save expiration info query task
            val expirationSource: TaskCompletionSource<Any> = TaskCompletionSource()
            //to save sync strategy info query task
            val syncSource: TaskCompletionSource<Any> = TaskCompletionSource()

            refDbExpiration.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    expirationSource.setResult(p0)
                }

                override fun onCancelled(p0: DatabaseError) {
                    expirationSource.setResult(p0)
                }
            })
            refDbSyncStrategy.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    syncSource.setResult(p0)
                }

                override fun onCancelled(p0: DatabaseError) {
                    syncSource.setResult(p0)
                }
            })

            try {
                //get expiration
                val expirationTask = expirationSource.task
                //this code bellow can throw an exception
                expirationTask.await()
                //get sync
                val syncTask = syncSource.task
                //this code bellow can throw an exception
                syncTask.await()

                //lastUpdated and customId are localed in users level on firebase
                val syncDataSnapshot = (syncTask.result as DataSnapshot)
                val expirationDataSnapshot = (expirationTask.result as DataSnapshot)
                Timber.d("$syncDataSnapshot")
                Timber.d("$expirationDataSnapshot")
                val customId =
                    syncDataSnapshot.child(REMOTE_DATABASE_CUSTOM_ID_KEY).value.toString()

                val lastUpdated =
                    syncDataSnapshot.child(DATABASE_LAST_UPDATED_KEY).value.toString()

                //expiration is localed on app level directory on firebase
                val expiration =
                    expirationDataSnapshot.child("$DATABASE_APP_LEVEL_STRATEGY_KEY/$DATABASE_APP_LEVEL_EXPIRATION_KEY").value.toString()
                return@withContext SyncStrategy(
                    expiration = expiration.toLong(),
                    lastUpdated = lastUpdated.toLong(),
                    customId = customId.toLong()
                )
            } catch (e: Exception) {
                Timber.d(e.printStackTrace().toString())
                throw e
            }
        }
    }

    override suspend fun addPhotosDoc(remoteId: Long, photos: List<Photo>, newJsonPages: String) {
        val userId: String =
            auth.currentUser?.uid
                ?: throw Exception(context.getString(R.string.common_user_not_logged_in))
        //Firebase Storage
        val fireStorage = storage.reference
        //send all photos to cloud
        photos.forEach { photo ->
            //reference to save image into Storage
            val refStorageImage = fireStorage.child(
                "$STORAGE_USERS_DIRECTORY/$userId/$STORAGE_IMAGES_DIRECTORY/${remoteId}_${photo.id}.jpg"
            )
            //this code bellow can throw an exception
            //use a try catch block when invoke this function
            try {
                val uriFile = Uri.fromFile(File(photo.path))
                val task1 = refStorageImage.putFile(uriFile)
                task1.await()
            } catch (e: Exception) {
                throw Exception(e)
            }
        }
        //update database with new photo ids
        //Firebase Database
        val database = database.reference
        //reference to save values into database
        val refDatabase =
            database.child("$DATABASE_USERS_DIRECTORY/$userId/$DATABASE_DOCUMENTS_DIRECTORY/$remoteId")
        //update jsonPages on Real Database
        val mapDatabase: HashMap<String, Any> = HashMap()
        mapDatabase[DATABASE_JSON_PAGES_KEY] = newJsonPages
        try {
            val task = refDatabase.updateChildren(mapDatabase)
            task.await()
        } catch (e: Exception) {
            throw Exception(e)
        }
    }

    override suspend fun sendCustomIdForceUpdate(customId: Long) {
        withContext(dispatcher) {
            //send customId to database
            val userId: String =
                auth.currentUser?.uid
                    ?: throw Exception(context.getString(R.string.common_user_not_logged_in))
            //Firebase Database
            val database = database.reference
            //reference to save values into database
            val refDatabase =
                database.child("$DATABASE_USERS_DIRECTORY/$userId/$DATABASE_SYNC_STRATEGY_KEY")

            //save into Real Database
            //set lastUpdated (field from Sync Strategy Model) to 0 (TIMESTAMP) (See SyncDataWorker)
            //set customId

            val mapDatabase: HashMap<String, String> = hashMapOf(
                REMOTE_DATABASE_CUSTOM_ID_KEY to customId.toString(),
                DATABASE_LAST_UPDATED_KEY to 0L.toString()
            )
            try {
                val sendValuesTask = refDatabase.setValue(mapDatabase)
                sendValuesTask.await()
            } catch (e: Exception) {
                throw Exception(context.getString(R.string.common_error_send_custom_id_to_server))
            }
        }
    }

    override suspend fun generatePDFLink(file: File, customId: Long): Uri {
        return withContext(dispatcher) {
            val userId: String = auth.currentUser?.uid
                ?: throw Exception(context.getString(R.string.common_user_not_logged_in))
            //Firebase Storage
            val fireStorage = storage.reference
            //reference to save pdf file into Storage
            val refStorage =
                fireStorage.child("$STORAGE_USERS_DIRECTORY/$userId/$STORAGE_TEMP_DIRECTORY/${customId}.pdf")

            //can throw an exception
            val uriFile = Uri.fromFile(file)
            val sendFileTask = refStorage.putFile(uriFile)
            sendFileTask.await()

            //download uri
            val downloadUriTask = refStorage.downloadUrl
            downloadUriTask.await()
            //pdf uri
            return@withContext downloadUriTask.result
        }
    }

    override suspend fun removeTempFile(customId: Long) {
        withContext(dispatcher) {
            val userId: String =
                auth.currentUser?.uid
                    ?: throw Exception(context.getString(R.string.common_user_not_logged_in))
            //Firebase Storage
            val fireStorage = storage.reference

            //reference to delete pdf file
            val refStorageImage = fireStorage.child(
                "$STORAGE_USERS_DIRECTORY/$userId/$STORAGE_TEMP_DIRECTORY/${customId}.pdf"
            )
            //can throw an exception
            val task1 = refStorageImage.delete()
            task1.await()
        }
    }

    override suspend fun getRemoveTempFileTime(): Long {
        return withContext(dispatcher){
            //Firebase Database
            val database = database.reference
            //reference to get exclude temp file time info
            val refDbTempFileTime = database.child(DATABASE_USERS_DIRECTORY)

            //to save expiration info query task
            val tempFileTimeSource: TaskCompletionSource<Any> = TaskCompletionSource()

            refDbTempFileTime.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    tempFileTimeSource.setResult(p0)
                }

                override fun onCancelled(p0: DatabaseError) {
                    tempFileTimeSource.setResult(p0)
                }
            })

            val tempFileTimeTask = tempFileTimeSource.task
            //this code bellow can throw an exception
            tempFileTimeTask.await()

            val tempFileTimeDataSnapshot = (tempFileTimeTask.result as DataSnapshot)
            return@withContext tempFileTimeDataSnapshot
                .child("$DATABASE_APP_LEVEL_STRATEGY_KEY/$DATABASE_APP_LEVEL_EXCLUDE_TEMP_TIME_KEY")
                    .value
                    .toString()
                    .toLong()
        }
    }

    private suspend fun getPages(ids: List<Long>, userId: String, remoteId: String): List<Photo> {
        //Firebase Storage
        val fireStorage = storage.reference
        val pages = ArrayList<Photo>()
        ids.forEach { id ->
            //reference to delete images from Storage
            val refStorageImage = fireStorage.child(
                "$STORAGE_USERS_DIRECTORY/$userId/$STORAGE_IMAGES_DIRECTORY/${remoteId}_${id}.jpg"
            )

            val tempLocalFile = File.createTempFile("images", "jpg")

            val task = refStorageImage.getFile(tempLocalFile)
            task.await()
            if (task.isSuccessful) {
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
            } else {
                Timber.d("failure on download file. \nDetails: ${task.exception}")
            }
        }
        return pages
    }
}