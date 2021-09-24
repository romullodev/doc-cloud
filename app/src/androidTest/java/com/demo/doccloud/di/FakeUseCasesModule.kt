package com.demo.doccloud.di

import com.demo.doccloud.*
import com.demo.doccloud.domain.di.UseCaseModule
import com.demo.doccloud.domain.usecases.contracts.*
import com.demo.doccloud.domain.usecases.impl.*
import dagger.Binds
import dagger.Module
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.testing.TestInstallIn

//not all use cases are fake. only those ones that is needed
@TestInstallIn(
    components = [ViewModelComponent::class],
    replaces = [UseCaseModule::class]
)
@Module
abstract class FakeUseCaseModule {

    @Binds
    abstract fun bindCopyFileUseCase(copyFile: FakeCopyFileImpl): CopyFile

    @Binds
    abstract fun bindGenerateDocPdfUseCase(generateDocPdf: FakeGenerateDocPdfImpl): GenerateDocPdf

    //used inside UpdatedDocName
    @Binds
    abstract fun bindScheduleToUpdateDocNameUseCase(schedule: FakeScheduleToUpdateRemoteDocNameImpl): ScheduleToUpdateRemoteDocName

    @Binds
    abstract fun bindScheduleToDeleteRemoteDocPhotoUseCase(schedule: FakeScheduleToDeleteRemoteDocPhotoImpl): ScheduleToDeleteRemoteDocPhoto

    @Binds
    abstract fun bindScheduleToUpdateRemoteDocPhotoUseCase(schedule: FakeScheduleToUpdateRemoteDocPhotoImpl): ScheduleToUpdateRemoteDocPhoto

    @Binds
    abstract fun bindScheduleToSyncDataUseCase(schedule: FakeScheduleToSyncDataImpl): ScheduleToSyncData

    @Binds
    abstract fun bindScheduleToDeleteRemoteDocUseCase(deleteLocalDoc: FakeScheduleToDeleteRemoteDocImpl): ScheduleToDeleteRemoteDoc

    @Binds
    abstract fun bindScheduleToAddRemoteDocPhotosUseCase(schedule: FakeScheduleToAddRemoteDocPhotosImpl): ScheduleToAddRemoteDocPhotos

    @Binds
    abstract fun bindScheduleToSaveRemoteDocUseCase(schedule: FakeScheduleToSaveRemoteDocImpl): ScheduleToSaveRemoteDoc

    //used inside UpdatedDocName
    @Binds
    abstract fun bindUpdateDocNameOnDeviceUseCase(schedule: UpdateLocalDocNameImpl): UpdateLocalDocName

    @Binds
    abstract fun bindUpdatedDocNameUseCase(updatedDocNameImpl: UpdatedDocNameImpl): UpdatedDocName

    @Binds
    abstract fun bindDeleteLocalDocPhotoUseCase(updatedDocNameImpl: DeleteLocalDocPhotoImpl): DeleteLocalDocPhoto

    @Binds
    abstract fun bindUpdateLocalDocPhotoUseCase(updateLocalDocPhoto: UpdateLocalDocPhotoImpl): UpdateLocalDocPhoto

    @Binds
    abstract fun bindUpdateDocPhotoUseCase(updateDocPhoto: UpdateDocPhotoImpl): UpdateDocPhoto

    @Binds
    abstract fun bindDeleteDocPhotoUseCase(deleteDocPhoto: DeleteDocPhotoImpl): DeleteDocPhoto

    @Binds
    abstract fun bindDoLogoutUseCase(doLogout: DoLogoutImpl): DoLogout

    @Binds
    abstract fun bindDeleteLocalDocUseCase(deleteLocalDoc: DeleteLocalDocImpl): DeleteLocalDoc

    @Binds
    abstract fun bindDeleteDocUseCase(deleteDoc: DeleteDocImpl): DeleteDoc

    @Binds
    abstract fun bindGetUserUseCase(getUser: GetUserImpl): GetUser


    @Binds
    abstract fun bindGetAllDocsUseCase(getAllDocs: GetAllDocsImpl): GetAllDocs

    @Binds
    abstract fun bindDoLoginWithGoogleUseCase(doLoginWithGoogle: DoLoginWithGoogleImpl): DoLoginWithGoogle

    @Binds
    abstract fun bindSaveLocalDocUseCase(saveLocalDoc: SaveLocalDocImpl): SaveLocalDoc

    @Binds
    abstract fun bindAddPhotosToLocalDocUseCase(addPhotosToLocalDoc: AddPhotosToLocalDocImpl): AddPhotosToLocalDoc

    @Binds
    abstract fun bindAddPhotosUseCase(addPhotos: AddPhotosImpl): AddPhotos

    @Binds
    abstract fun bindSaveDocImplUseCase(saveDocImpl: SaveDocImpl): SaveDoc

}