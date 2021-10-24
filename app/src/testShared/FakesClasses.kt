package com.demo.doccloud

import android.content.Context
import android.net.Uri
import com.demo.doccloud.di.MainDispatcher
import com.demo.doccloud.domain.entities.Doc
import com.demo.doccloud.domain.entities.Photo
import com.demo.doccloud.domain.usecases.contracts.*
import com.demo.doccloud.idling.wrapEspressoIdlingResource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import java.io.File
import javax.inject.Inject

/**
 * Fake classes to help on tests
 */
class FakeScheduleToDeleteRemoteDocImpl @Inject constructor(): ScheduleToDeleteRemoteDoc {
    override suspend fun invoke(remoteId: Long, jsonPages: String) {}
}

class FakeDeleteDoc(private val context: Context): DeleteDoc {
    override suspend fun invoke(doc: Doc): String {
        if(GlobalVariablesTest.shouldThrowException){
            throw Exception()
        }
        return context.getString(R.string.home_toast_delete_success, doc.name)
    }
}

class FakeUpdatedDocName : UpdatedDocName{
    override suspend fun invoke(localId: Long, remoteId: Long, name: String) {
        if(GlobalVariablesTest.shouldThrowException){
            throw Exception()
        }
    }
}

class FakeDeleteDocPhoto: DeleteDocPhoto{
    override suspend fun invoke(localId: Long, photo: Photo) {
        if(GlobalVariablesTest.shouldThrowException){
            throw Exception()
        }
    }
}

class FakeGenerateDocPdfImpl @Inject constructor(
    @MainDispatcher private val dispatcher: CoroutineDispatcher,
    @ApplicationContext private val context: Context
): GenerateDocPdf {
    override suspend fun invoke(doc: Doc): File {
        return wrapEspressoIdlingResource {
            return@wrapEspressoIdlingResource withContext(dispatcher){
                if(GlobalVariablesTest.shouldThrowException){
                    throw Exception()
                }
                if(GlobalVariablesTest.hasDelay)
                    delay(GlobalVariablesTest.delayDuration)

                return@withContext File("${context.filesDir.path}/tmp.txt")
            }
        }
    }
}
class FakeCopyFileImpl @Inject constructor(): CopyFile {
    override suspend fun invoke(uri: Uri): File {
        if(GlobalVariablesTest.shouldThrowException){
            throw Exception()
        }
        return GlobalVariablesTest.fakeFile
    }
}

class FakeScheduleToSyncDataImpl @Inject constructor(): ScheduleToSyncData {
    override suspend fun invoke() {}
}

class FakeScheduleToAddRemoteDocPhotosImpl @Inject constructor(): ScheduleToAddRemoteDocPhotos {
    override suspend fun invoke(localId: Long, photosId: List<Long>) {}
}

class FakeScheduleToDeleteRemoteDocPhotoImpl @Inject constructor(): ScheduleToDeleteRemoteDocPhoto {
    override suspend fun invoke(localId: Long, photo: Photo) {}
}
class FakeScheduleToSaveRemoteDocImpl @Inject constructor(): ScheduleToSaveRemoteDoc {
    override suspend fun invoke(rowNumber: Long) {}
}
class FakeScheduleToUpdateRemoteDocNameImpl @Inject constructor(): ScheduleToUpdateRemoteDocName {
    override suspend fun invoke(localId: Long, remoteId: Long, name: String) {}
}
class FakeScheduleToUpdateRemoteDocPhotoImpl @Inject constructor(): ScheduleToUpdateRemoteDocPhoto {
    override suspend fun invoke(localId: Long, photo: Photo) {}
}


