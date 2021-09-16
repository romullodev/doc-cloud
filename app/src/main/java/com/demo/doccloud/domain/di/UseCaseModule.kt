package com.demo.doccloud.domain.di

import com.demo.doccloud.domain.usecases.contracts.*
import com.demo.doccloud.domain.usecases.impl.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class UseCaseForWorkManagerModule{
    @Binds
    abstract fun bindGetDocByIdUseCase(getDocById: GetDocByIdImpl): GetDocById

    @Binds
    abstract fun bindUpdateLocalDocUseCase(updateLocalDoc: UpdateLocalDocImpl): UpdateLocalDoc

    @Binds
    abstract fun bindAddPhotoToRemoteDocUseCase(addPhotoToRemoteDoc: AddPhotoToRemoteDocImpl): AddPhotoToRemoteDoc

    @Binds
    abstract fun bindDeleteRemoteDocPhotoUseCase(deleteRemoteDocPhoto: DeleteRemoteDocPhotoImpl): DeleteRemoteDocPhoto

    @Binds
    abstract fun bindDeleteRemoteDocUseCase(deleteRemoteDoc: DeleteRemoteDocImpl): DeleteRemoteDoc

    @Binds
    abstract fun bindGetSavedCustomIdSyncStrategyUseCase(getSavedCustomIdSyncStrategy: GetSavedCustomIdSyncStrategyImpl): GetSavedCustomIdSyncStrategy

    @Binds
    abstract fun bindGetGetSyncStrategyUseCase(getSyncStrategy: GetSyncStrategyImpl): GetSyncStrategy

    @Binds
    abstract fun bindSyncDataUseCase(syncData: SyncDataImpl): SyncData

    @Binds
    abstract fun bindSaveCustomIdSyncStrategyUseCase(saveCustomIdSyncStrategy: SaveCustomIdSyncStrategyImpl): SaveCustomIdSyncStrategy

    @Binds
    abstract fun bindUpdateRemoteDocNameUseCase(updateRemoteDocName: UpdateRemoteDocNameImpl): UpdateRemoteDocName

    @Binds
    abstract fun bindUpdateRemoteDocPhotoUseCase(updateRemoteDocPhoto: UpdateRemoteDocPhotoImpl): UpdateRemoteDocPhoto

    @Binds
    abstract fun bindUploadDocUseCase(uploadDoc: UploadDocImpl): UploadDoc

}

@Module
@InstallIn(ViewModelComponent::class)
abstract class UseCaseModule {

    @Binds
    abstract fun bindCopyFileUseCase(copyFile: CopyFileImpl): CopyFile

    @Binds
    abstract fun bindGenerateDocPdfUseCase(generateDocPdf: GenerateDocPdfImpl): GenerateDocPdf

    //used inside UpdatedDocName
    @Binds
    abstract fun bindScheduleToUpdateDocNameUseCase(schedule: ScheduleToUpdateRemoteDocNameImpl): ScheduleToUpdateRemoteDocName

    //used inside UpdatedDocName
    @Binds
    abstract fun bindUpdateDocNameOnDeviceUseCase(schedule: UpdateLocalDocNameImpl): UpdateLocalDocName

    @Binds
    abstract fun bindUpdatedDocNameUseCase(updatedDocNameImpl: UpdatedDocNameImpl): UpdatedDocName

    @Binds
    abstract fun bindDeleteLocalDocPhotoUseCase(updatedDocNameImpl: DeleteLocalDocPhotoImpl): DeleteLocalDocPhoto

    @Binds
    abstract fun bindScheduleToDeleteRemoteDocPhotoUseCase(schedule: ScheduleToDeleteRemoteDocPhotoImpl): ScheduleToDeleteRemoteDocPhoto

    @Binds
    abstract fun bindUpdateLocalDocPhotoUseCase(updateLocalDocPhoto: UpdateLocalDocPhotoImpl): UpdateLocalDocPhoto

    @Binds
    abstract fun bindScheduleToUpdateRemoteDocPhotoUseCase(schedule: ScheduleToUpdateRemoteDocPhotoImpl): ScheduleToUpdateRemoteDocPhoto

    @Binds
    abstract fun bindUpdateDocPhotoUseCase(updateDocPhoto: UpdateDocPhotoImpl): UpdateDocPhoto

    @Binds
    abstract fun bindDeleteDocPhotoUseCase(deleteDocPhoto: DeleteDocPhotoImpl): DeleteDocPhoto

    @Binds
    abstract fun bindDoLogoutUseCase(doLogout: DoLogoutImpl): DoLogout

    @Binds
    abstract fun bindDeleteLocalDocUseCase(deleteLocalDoc: DeleteLocalDocImpl): DeleteLocalDoc

    @Binds
    abstract fun bindScheduleToDeleteRemoteDocUseCase(deleteLocalDoc: ScheduleToDeleteRemoteDocImpl): ScheduleToDeleteRemoteDoc

    @Binds
    abstract fun bindDeleteDocUseCase(deleteDoc: DeleteDocImpl): DeleteDoc

    @Binds
    abstract fun bindGetUserUseCase(getUser: GetUserImpl): GetUser

    @Binds
    abstract fun bindScheduleToSyncDataUseCase(schedule: ScheduleToSyncDataImpl): ScheduleToSyncData

    @Binds
    abstract fun bindGetAllDocsUseCase(getAllDocs: GetAllDocsImpl): GetAllDocs

    @Binds
    abstract fun bindDoLoginWithGoogleUseCase(doLoginWithGoogle: DoLoginWithGoogleImpl): DoLoginWithGoogle

    @Binds
    abstract fun bindSaveLocalDocUseCase(saveLocalDoc: SaveLocalDocImpl): SaveLocalDoc

    @Binds
    abstract fun bindAddPhotosToLocalDocUseCase(addPhotosToLocalDoc: AddPhotosToLocalDocImpl): AddPhotosToLocalDoc

    @Binds
    abstract fun bindScheduleToAddRemoteDocPhotosUseCase(schedule: ScheduleToAddRemoteDocPhotosImpl): ScheduleToAddRemoteDocPhotos

    @Binds
    abstract fun bindAddPhotosUseCase(addPhotos: AddPhotosImpl): AddPhotos

    @Binds
    abstract fun bindSaveDocImplUseCase(saveDocImpl: SaveDocImpl): SaveDoc

    @Binds
    abstract fun bindScheduleToSaveRemoteDocUseCase(schedule: ScheduleToSaveRemoteDocImpl): ScheduleToSaveRemoteDoc

}