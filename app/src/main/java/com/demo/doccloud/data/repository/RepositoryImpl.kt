package com.demo.doccloud.data.repository

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import androidx.lifecycle.LiveData
import androidx.work.*
import com.demo.doccloud.R
import com.demo.doccloud.data.datasource.local.LocalDataSource
import com.demo.doccloud.data.datasource.remote.RemoteDataSource
import com.demo.doccloud.di.IoDispatcher
import com.demo.doccloud.domain.Doc
import com.demo.doccloud.domain.DocStatus
import com.demo.doccloud.domain.Photo
import com.demo.doccloud.domain.User
import com.demo.doccloud.utils.AppConstants
import com.demo.doccloud.utils.Global
import com.demo.doccloud.utils.Result
import com.demo.doccloud.workers.*
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RepositoryImpl @Inject constructor(
    private val remoteDatasource: RemoteDataSource,
    private val localDatasource: LocalDataSource,
    @ApplicationContext private val context: Context,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : Repository {
    override val docs: LiveData<List<Doc>> get() = localDatasource.getSavedDocs()

    override suspend fun doLoginWithGoogle(data: Intent?) : Result<User>{
        val result = localDatasource.saveCustomId()
        return when(result.status){
            Result.Status.SUCCESS -> {
                remoteDatasource.doLoginWithGoogle(data, result.data!!)
            }
            Result.Status.ERROR -> {
                //this error will be shown on Ui via Alert Dialog
                Result.error(result.msg!!)
            }
        }
    }

    override suspend fun getUser() = remoteDatasource.getUser()

    override suspend fun doLogout() : Result<Boolean>{
        localDatasource.clearAllData()
        return remoteDatasource.doLogout()
    }

    override suspend fun saveDoc(doc: Doc): Result<Boolean> {
        val rowNumber: Long = localDatasource.saveDocOnDevice(doc)
        //schedule to send docs to server
        setupSendDocSchedule(rowNumber)
        return Result.success(true)
    }

    override suspend fun deleteDoc(doc: Doc): Result<String> {
        localDatasource.deleteDocOnDevice(doc)
        //schedule to delete docs from server
        setupDeleteDocSchedule(doc.remoteId, Gson().toJson(doc.pages))
        return Result.success(context.getString(R.string.home_toast_delete_success, doc.name))
    }

    override suspend fun getDoc(id: Long): Result<Doc> {
        val doc = localDatasource.getDoc(id)
        return Result.success(doc)
    }

    override suspend fun updateDocName(localId: Long, remoteId: Long, name: String) {
        localDatasource.updateDocName(localId, name)
        val doc = localDatasource.getDoc(localId)
        localDatasource.updateDoc(
            doc.copy(status = DocStatus.NOT_SENT)
        )
        setupUpdateDocNameSchedule(localId = localId, remoteId = remoteId, name = name)
    }

    override suspend fun deleteDocPhoto(localId: Long, remoteId: Long, photo: Photo) {
        localDatasource.deleteDocPhoto(
            localId = localId,
            photo = photo
        )
        val doc = localDatasource.getDoc(localId)
        localDatasource.updateDoc(
            doc.copy(status = DocStatus.NOT_SENT)
        )
        setupDeleteDocPhotoSchedule(localId, photo)
    }
    override suspend fun scheduleToSyncData() {
        //setup constraint to workManager (only send if network is available)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        //setup the request work to send
        val syncData =
            OneTimeWorkRequestBuilder<SyncDataWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

        //schedule the work to be done
        WorkManager.getInstance(context).enqueue(syncData)
    }

    override suspend fun generatePdf(doc: Doc): Result<File> {
        return withContext(dispatcher){
            val dir = Global.getInternalOutputDirectory(context)
            //if (!dir.exists()) {
            //    dir.mkdir()
            //}
            //save pdf
            var bitmap: Bitmap
            var pageInfo: PdfDocument.PageInfo
            val document = PdfDocument()
            var page: PdfDocument.Page
            var canvas: Canvas
            //calculates the larger width amongst photos
            var majorWidth = Int.MIN_VALUE
            doc.pages.forEach {
                bitmap = BitmapFactory.decodeFile(it.path)
                if(bitmap.width > majorWidth)
                    majorWidth = bitmap.width
            }

            //create pdf with photos
            for ((index, path) in doc.pages.withIndex()) {
                bitmap = BitmapFactory.decodeFile(path.path)
                //bitmap = ImageResizer.reduceBitmapSize(bitmap, MAX_SIZE_EACH_PHOTO_DOCUMENT)
                pageInfo =
                    PdfDocument.PageInfo.Builder(majorWidth, bitmap.height, index + 1) //A4 resolution
                        .create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                //bitmap = Bitmap.createScaledBitmap(
                //    bitmap,
                //    2480,
                //    3508,
                //    true
                //)
                val startPointCenter = (majorWidth - bitmap.width)/2.0f
                canvas.drawBitmap(bitmap, startPointCenter, 0f, null)
                document.finishPage(page)
            }
            // Create the pdf name + timeStamp
            val formattedTimeStamp = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis())
            val pdfFilePath = "${dir.path}/${doc.name} ${formattedTimeStamp}.pdf"
            // write the document content
            val pdfFile = File(pdfFilePath)
            //this code bellow can throw an exception (up to document.close())
            //use a try catch block when invoke this function
            try {
                document.writeTo(FileOutputStream(pdfFile))
                document.close()
                return@withContext Result.success(pdfFile)
            }catch (e: Exception){
                return@withContext Result.error("Failure on save pdf file on the device. \nDetails: $e")
            }
        }
    }

    override suspend fun addPhotos(photos: List<Photo>, localId: Long) {
        localDatasource.addPhotosToDoc(localId = localId, photos = photos)
        setupAddPhotoDocSchedule(localId = localId, photosId = photos.map { it.id })
    }

    private fun setupAddPhotoDocSchedule(localId: Long, photosId: List<Long>) {
        val data =
            workDataOf(
                AppConstants.LOCAL_ID_KEY to localId,
                AppConstants.LIST_PHOTO_ADD_KEY to Gson().toJson(photosId),
                )

        //setup constraint to workManager (only send if network is available)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        //setup the request work to send
        val addPhotosWorkRequest =
            OneTimeWorkRequestBuilder<AddDocPhotosWorker>()
                .setInputData(data)
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

        //schedule the work to be done
        WorkManager.getInstance(context).enqueue(addPhotosWorkRequest)
    }

    override suspend fun updateDocPhotos(localId: Long, remoteId: Long, photo: Photo) {
        localDatasource.updateDocPhoto(localId = localId, photo = photo)
        val doc = localDatasource.getDoc(localId)
        localDatasource.updateDoc(
            doc.copy(status = DocStatus.NOT_SENT)
        )
        setupUpdateDocPhotosSchedule(localId = localId, photo = photo)
    }

    private fun setupDeleteDocPhotoSchedule(localId: Long, photo: Photo){
        val data =
            workDataOf(
                AppConstants.LOCAL_ID_KEY to localId,
                AppConstants.PHOTO_ID_KEY to photo.id,
                AppConstants.PHOTO_PATH_KEY to photo.path
                )

        //setup constraint to workManager (only send if network is available)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        //setup the request work to send
        val deleteDocPhotoWorkRequest =
            OneTimeWorkRequestBuilder<DeleteDocPageWorker>()
                .setInputData(data)
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

        //schedule the work to be done
        WorkManager.getInstance(context).enqueue(deleteDocPhotoWorkRequest)
    }

    private fun setupUpdateDocNameSchedule(localId: Long, remoteId: Long, name: String) {
        val data =
            workDataOf(
                AppConstants.LOCAL_ID_KEY to localId,
                AppConstants.REMOTE_ID_KEY to remoteId,
                AppConstants.DOC_NAME_ID_KEY to name,

                )

        //setup constraint to workManager (only send if network is available)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        //setup the request work to send
        val updateDocNameWorkRequest =
            OneTimeWorkRequestBuilder<UpdateDocNameWorker>()
                .setInputData(data)
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .addTag("$remoteId")
                .build()

        //cancel a schedule work in case it has been previous setup by this same remoteId tag
        //this code could be improved
        WorkManager.getInstance(context).cancelAllWorkByTag("$remoteId")

        //schedule the work to be done
        WorkManager.getInstance(context).enqueue(updateDocNameWorkRequest)
    }

    private fun setupUpdateDocPhotosSchedule(localId: Long, photo: Photo) {
        val data =
            workDataOf(
                AppConstants.LOCAL_ID_KEY to localId,
                AppConstants.PHOTO_ID_KEY to photo.id,
                AppConstants.PHOTO_PATH_KEY to photo.path,
            )

        //setup constraint to workManager (only send if network is available)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        //setup the request work to send
        val updateDocNameWorkRequest =
            OneTimeWorkRequestBuilder<UpdateDocPageWorker>()
                .setInputData(data)
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .addTag("${localId}_${photo.id}")
                .build()
        //cancel a schedule work in case it has been previous setup by this same localId + photo tag
        //this code could be improved
        WorkManager.getInstance(context).cancelAllWorkByTag("${localId}_${photo.id}")

        //schedule the work to be done
        WorkManager.getInstance(context).enqueue(updateDocNameWorkRequest)
    }

    private fun setupSendDocSchedule(rowNumber: Long) {
        val data =
            workDataOf(
                AppConstants.LOCAL_ID_KEY to rowNumber,
            )

        //setup constraint to workManager (only send if network is available)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        //setup the request work to send
        val uploadWorkRequest =
            OneTimeWorkRequestBuilder<UploadDocWorker>()
                .setInputData(data)
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

        //schedule the work to be done
        WorkManager.getInstance(context).enqueue(uploadWorkRequest)
    }

    private fun setupDeleteDocSchedule(remoteId: Long, jsonPages: String) {
        val data =
            workDataOf(
                AppConstants.REMOTE_ID_KEY to remoteId,
                AppConstants.JSON_PAGES_KEY to jsonPages,
            )

        //setup constraint to workManager (only send if network is available)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        //setup the request work to send
        val deleteDocWorkRequest =
            OneTimeWorkRequestBuilder<DeleteDocWorker>()
                .setInputData(data)
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

        //schedule the work to be done
        WorkManager.getInstance(context).enqueue(deleteDocWorkRequest)

    }
}