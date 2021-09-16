package com.demo.doccloud.fakes

import android.net.Uri
import com.demo.doccloud.GlobalVariablesTest
import com.demo.doccloud.domain.entities.Doc
import com.demo.doccloud.domain.entities.Photo
import com.demo.doccloud.domain.usecases.contracts.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.io.File

/**
 * Fake classes to help on tests
 */
class FakeScheduleToDeleteRemoteDocImpl : ScheduleToDeleteRemoteDoc {
    override suspend fun invoke(remoteId: Long, jsonPages: String) {}
}
class FakeGenerateDocPdfImpl: GenerateDocPdf {
    @ExperimentalCoroutinesApi
    override suspend fun invoke(doc: Doc): File {
        if(GlobalVariablesTest.shouldThrowException){
            throw Exception()
        }
        return GlobalVariablesTest.fakeFile
    }
}
class FakeCopyFileImpl: CopyFile {
    override suspend fun invoke(uri: Uri): File? {
        if(GlobalVariablesTest.shouldThrowException){
            throw Exception()
        }
        return GlobalVariablesTest.fakeFile
    }
}

class FakeScheduleToSyncDataImpl: ScheduleToSyncData {
    override suspend fun invoke() {}
}

class FakeScheduleToAddRemoteDocPhotosImpl : ScheduleToAddRemoteDocPhotos {
    override suspend fun invoke(localId: Long, photosId: List<Long>) {}
}

class FakeScheduleToDeleteRemoteDocPhotoImpl : ScheduleToDeleteRemoteDocPhoto {
    override suspend fun invoke(localId: Long, photo: Photo) {}
}
class FakeScheduleToSaveRemoteDocImpl : ScheduleToSaveRemoteDoc {
    override suspend fun invoke(rowNumber: Long) {}
}
class FakeScheduleToUpdateRemoteDocNameImpl : ScheduleToUpdateRemoteDocName {
    override suspend fun invoke(localId: Long, remoteId: Long, name: String) {}
}
class FakeScheduleToUpdateRemoteDocPhotoImpl : ScheduleToUpdateRemoteDocPhoto {
    override suspend fun invoke(localId: Long, photo: Photo) {}
}


