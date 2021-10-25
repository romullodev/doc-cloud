package com.demo.doccloud.ui

import android.content.Context
import androidx.room.Room
import com.demo.doccloud.FakeCopyFileImpl
import com.demo.doccloud.data.datasource.local.AppLocalServices
import com.demo.doccloud.data.datasource.local.LocalDataSource
import com.demo.doccloud.data.datasource.local.persist.SharedPreferenceImpl
import com.demo.doccloud.data.datasource.local.room.AppDatabase
import com.demo.doccloud.data.repository.Repository
import com.demo.doccloud.domain.entities.Doc
import com.demo.doccloud.domain.entities.DocStatus
import com.demo.doccloud.domain.entities.Photo
import com.demo.doccloud.domain.entities.User
import com.demo.doccloud.domain.usecases.contracts.*
import com.demo.doccloud.domain.usecases.impl.*
import com.demo.doccloud.ui.crop.CropViewModel
import com.demo.doccloud.ui.edit.EditViewModel
import com.demo.doccloud.ui.home.HomeViewModel
import kotlinx.coroutines.Dispatchers

class AndroidTestUtil {
    companion object{
        fun getUser() = User("any","any")

        fun getRealDoc(context: Context) = Doc(
            remoteId = 1L,
            localId = 1L,
            name = "doc 1",
            date = "--/--/----",
            pages = listOf(Photo(id = 1L, path = FileUtil.getStubFile(context).path)),
            status = DocStatus.SENT,
        )

        fun getRealDocs(context: Context): List<Doc>{
            val doc = getRealDoc(context)
            return listOf<Doc>(
                doc,
                doc.copy(remoteId = 2L, name = "doc 2"),
                doc.copy(remoteId = 3L, name = "doc 3"),
                doc.copy(remoteId = 4L, name = "doc 4")
            )

        }

        fun getHomeViewModelWithMockGeneratePdfUseCase(context: Context, repository: Repository, generateDocPdf: GenerateDocPdf): HomeViewModel{
            val copyFileUseCase = CopyFileImpl(context, Dispatchers.Default)
            val doLogoutUseCase = DoLogoutImpl(repository)
            val scheduleToDeleteRemoteDoc = ScheduleToDeleteRemoteDocImpl(context)
            val deleteLocalDoc = DeleteLocalDocImpl(repository)
            val deleteDocUseCase = DeleteDocImpl(deleteLocalDoc, scheduleToDeleteRemoteDoc, context)
            val getUserUseCase = GetUserImpl(repository)
            val scheduleToSyncData = ScheduleToSyncDataImpl(context)
            val scheduleToRemoveTempFile = ScheduleToRemoveTempFileImpl(context)
            val getRemoveTempFileTime = GetRemoveTempFileTimeImpl(repository)
            val generatePDFLink = GeneratePDFLinkImpl(generateDocPdf, scheduleToRemoveTempFile, getRemoveTempFileTime, repository)
            val getAllDocsUse = GetAllDocsImpl(repository)

            return HomeViewModel(
                copyFileUseCase,
                generateDocPdf,
                doLogoutUseCase,
                deleteDocUseCase,
                getUserUseCase,
                scheduleToSyncData,
                generatePDFLink,
                getAllDocsUse
            )
        }

        fun getHomeViewModelWithMockDeleteDocUseCase(context: Context, repository: Repository, deleteDocUseCase: DeleteDoc): HomeViewModel{
            val copyFileUseCase = CopyFileImpl(context, Dispatchers.Default)
            val generateDocPdfUseCase = GenerateDocPdfImpl(context, Dispatchers.Main)
            val doLogoutUseCase = DoLogoutImpl(repository)
            val getUserUseCase = GetUserImpl(repository)
            val scheduleToSyncData = ScheduleToSyncDataImpl(context)
            val scheduleToRemoveTempFile = ScheduleToRemoveTempFileImpl(context)
            val getRemoveTempFileTime = GetRemoveTempFileTimeImpl(repository)
            val generatePDFLink = GeneratePDFLinkImpl(generateDocPdfUseCase, scheduleToRemoveTempFile, getRemoveTempFileTime, repository)
            val getAllDocsUse = GetAllDocsImpl(repository)

            return HomeViewModel(
                copyFileUseCase,
                generateDocPdfUseCase,
                doLogoutUseCase,
                deleteDocUseCase,
                getUserUseCase,
                scheduleToSyncData,
                generatePDFLink,
                getAllDocsUse
            )
        }

        fun getHomeViewModel(context: Context, repository: Repository): HomeViewModel{
            val copyFileUseCase = CopyFileImpl(context, Dispatchers.Default)
            val generateDocPdfUseCase = GenerateDocPdfImpl(context, Dispatchers.Main)
            val doLogoutUseCase = DoLogoutImpl(repository)
            val scheduleToDeleteRemoteDoc = ScheduleToDeleteRemoteDocImpl(context)
            val deleteLocalDoc = DeleteLocalDocImpl(repository)
            val deleteDocUseCase = DeleteDocImpl(deleteLocalDoc, scheduleToDeleteRemoteDoc, context)
            val getUserUseCase = GetUserImpl(repository)
            val scheduleToSyncData = ScheduleToSyncDataImpl(context)
            val scheduleToRemoveTempFile = ScheduleToRemoveTempFileImpl(context)
            val getRemoveTempFileTime = GetRemoveTempFileTimeImpl(repository)
            val generatePDFLink = GeneratePDFLinkImpl(generateDocPdfUseCase, scheduleToRemoveTempFile, getRemoveTempFileTime, repository)
            val getAllDocsUse = GetAllDocsImpl(repository)

            return HomeViewModel(
                copyFileUseCase,
                generateDocPdfUseCase,
                doLogoutUseCase,
                deleteDocUseCase,
                getUserUseCase,
                scheduleToSyncData,
                generatePDFLink,
                getAllDocsUse
            )
        }

        fun getEditViewModel(context: Context, repository: Repository): EditViewModel {
            val dispatcher = Dispatchers.Default
            val copyFileUseCase = CopyFileImpl(context, dispatcher)
            val generateDocPdfUseCase = GenerateDocPdfImpl(context, dispatcher)
            val getDocByIdUseCase = GetDocByIdImpl(repository)
            val scheduleToUpdateRemoteDocName = ScheduleToUpdateRemoteDocNameImpl(context)
            val updateLocalDocName = UpdateLocalDocNameImpl(repository)
            val updatedDocNameUseCase = UpdatedDocNameImpl(scheduleToUpdateRemoteDocName, updateLocalDocName)
            val deleteLocalDocPhoto = DeleteLocalDocPhotoImpl(repository)
            val scheduleToDeleteRemoteDocPhoto = ScheduleToDeleteRemoteDocPhotoImpl(context)
            val deleteDocPhotoUseCase = DeleteDocPhotoImpl(deleteLocalDocPhoto, scheduleToDeleteRemoteDocPhoto)
            val updateLocalDocPhoto = UpdateLocalDocPhotoImpl(repository)
            val scheduleToUpdateRemoteDocPhoto = ScheduleToUpdateRemoteDocPhotoImpl(context)
            val updateDocPhoto = UpdateDocPhotoImpl(updateLocalDocPhoto, scheduleToUpdateRemoteDocPhoto)

            return EditViewModel(
                copyFileUseCase,
                generateDocPdfUseCase,
                getDocByIdUseCase,
                updatedDocNameUseCase,
                deleteDocPhotoUseCase,
                updateDocPhoto
            )
        }

        fun getEditViewModelWithMockUpdatedDocName(context: Context, repository: Repository, updatedDocName: UpdatedDocName): EditViewModel{
            val dispatcher = Dispatchers.Default
            val copyFileUseCase = CopyFileImpl(context, dispatcher)
            val generateDocPdfUseCase = GenerateDocPdfImpl(context, dispatcher)
            val getDocByIdUseCase = GetDocByIdImpl(repository)
            val deleteLocalDocPhoto = DeleteLocalDocPhotoImpl(repository)
            val scheduleToDeleteRemoteDocPhoto = ScheduleToDeleteRemoteDocPhotoImpl(context)
            val deleteDocPhotoUseCase = DeleteDocPhotoImpl(deleteLocalDocPhoto, scheduleToDeleteRemoteDocPhoto)
            val updateLocalDocPhoto = UpdateLocalDocPhotoImpl(repository)
            val scheduleToUpdateRemoteDocPhoto = ScheduleToUpdateRemoteDocPhotoImpl(context)
            val updateDocPhoto = UpdateDocPhotoImpl(updateLocalDocPhoto, scheduleToUpdateRemoteDocPhoto)

            return EditViewModel(
                copyFileUseCase,
                generateDocPdfUseCase,
                getDocByIdUseCase,
                updatedDocName,
                deleteDocPhotoUseCase,
                updateDocPhoto
            )
        }

        fun getEditViewModelWithMockGeneratePdfDoc(context: Context, repository: Repository, generateDocPdf: GenerateDocPdf): EditViewModel{
            val dispatcher = Dispatchers.Default
            val copyFileUseCase = CopyFileImpl(context, dispatcher)
            val getDocByIdUseCase = GetDocByIdImpl(repository)
            val scheduleToUpdateRemoteDocName = ScheduleToUpdateRemoteDocNameImpl(context)
            val updateLocalDocName = UpdateLocalDocNameImpl(repository)
            val updatedDocNameUseCase = UpdatedDocNameImpl(scheduleToUpdateRemoteDocName, updateLocalDocName)
            val deleteLocalDocPhoto = DeleteLocalDocPhotoImpl(repository)
            val scheduleToDeleteRemoteDocPhoto = ScheduleToDeleteRemoteDocPhotoImpl(context)
            val deleteDocPhotoUseCase = DeleteDocPhotoImpl(deleteLocalDocPhoto, scheduleToDeleteRemoteDocPhoto)
            val updateLocalDocPhoto = UpdateLocalDocPhotoImpl(repository)
            val scheduleToUpdateRemoteDocPhoto = ScheduleToUpdateRemoteDocPhotoImpl(context)
            val updateDocPhoto = UpdateDocPhotoImpl(updateLocalDocPhoto, scheduleToUpdateRemoteDocPhoto)

            return EditViewModel(
                copyFileUseCase,
                generateDocPdf,
                getDocByIdUseCase,
                updatedDocNameUseCase,
                deleteDocPhotoUseCase,
                updateDocPhoto
            )
        }

        fun getEditViewModelWithMockFakeCopyFile(context: Context, repository: Repository, fakeCopyFile: CopyFile): EditViewModel{
            val dispatcher = Dispatchers.Default
            val generateDocPdfUseCase = GenerateDocPdfImpl(context, dispatcher)
            val getDocByIdUseCase = GetDocByIdImpl(repository)
            val scheduleToUpdateRemoteDocName = ScheduleToUpdateRemoteDocNameImpl(context)
            val updateLocalDocName = UpdateLocalDocNameImpl(repository)
            val updatedDocNameUseCase = UpdatedDocNameImpl(scheduleToUpdateRemoteDocName, updateLocalDocName)
            val deleteLocalDocPhoto = DeleteLocalDocPhotoImpl(repository)
            val scheduleToDeleteRemoteDocPhoto = ScheduleToDeleteRemoteDocPhotoImpl(context)
            val deleteDocPhotoUseCase = DeleteDocPhotoImpl(deleteLocalDocPhoto, scheduleToDeleteRemoteDocPhoto)
            val updateLocalDocPhoto = UpdateLocalDocPhotoImpl(repository)
            val scheduleToUpdateRemoteDocPhoto = ScheduleToUpdateRemoteDocPhotoImpl(context)
            val updateDocPhoto = UpdateDocPhotoImpl(updateLocalDocPhoto, scheduleToUpdateRemoteDocPhoto)

            return EditViewModel(
                fakeCopyFile,
                generateDocPdfUseCase,
                getDocByIdUseCase,
                updatedDocNameUseCase,
                deleteDocPhotoUseCase,
                updateDocPhoto
            )
        }

        fun getEditViewModelWithMockDeleteDocPhoto(context: Context, repository: Repository, deleteDocPhoto: DeleteDocPhoto): EditViewModel{
            val dispatcher = Dispatchers.Default
            val copyFileUseCase = CopyFileImpl(context, dispatcher)
            val generateDocPdfUseCase = GenerateDocPdfImpl(context, dispatcher)
            val getDocByIdUseCase = GetDocByIdImpl(repository)
            val scheduleToUpdateRemoteDocName = ScheduleToUpdateRemoteDocNameImpl(context)
            val updateLocalDocName = UpdateLocalDocNameImpl(repository)
            val updatedDocNameUseCase = UpdatedDocNameImpl(scheduleToUpdateRemoteDocName, updateLocalDocName)
            val updateLocalDocPhoto = UpdateLocalDocPhotoImpl(repository)
            val scheduleToUpdateRemoteDocPhoto = ScheduleToUpdateRemoteDocPhotoImpl(context)
            val updateDocPhoto = UpdateDocPhotoImpl(updateLocalDocPhoto, scheduleToUpdateRemoteDocPhoto)

            return EditViewModel(
                copyFileUseCase,
                generateDocPdfUseCase,
                getDocByIdUseCase,
                updatedDocNameUseCase,
                deleteDocPhoto,
                updateDocPhoto
            )
        }

        fun getLocalDataSource(context: Context): AppLocalServices {
            val dispatcher = Dispatchers.Default
            val appDatabase =
                Room.databaseBuilder(context, AppDatabase::class.java, "app_database").build()
            val persistSimpleData = SharedPreferenceImpl(context, dispatcher)
            return AppLocalServices(dispatcher, appDatabase, persistSimpleData)
        }

        fun getCropViewModel(context: Context, repository: Repository): CropViewModel{
            val saveLocalDoc = SaveLocalDocImpl(repository)
            val scheduleToSaveRemoteDocImpl = ScheduleToSaveRemoteDocImpl(context)
            val saveDocUseCase = SaveDocImpl(saveLocalDoc, scheduleToSaveRemoteDocImpl)

            val addPhotosToLocalDoc = AddPhotosToLocalDocImpl(repository)
            val scheduleToAddRemoteDocPhotos = ScheduleToAddRemoteDocPhotosImpl(context)
            val addPhotosUseCase = AddPhotosImpl(addPhotosToLocalDoc, scheduleToAddRemoteDocPhotos)

            val copyFileUseCase = CopyFileImpl(context, Dispatchers.Main)

            return CropViewModel(
                saveDocUseCase,
                addPhotosUseCase,
                copyFileUseCase
            )
        }
    }
}