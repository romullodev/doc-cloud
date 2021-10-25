package com.demo.doccloud.domain.di

import com.demo.doccloud.domain.usecases.contracts.*
import com.demo.doccloud.domain.usecases.impl.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
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

    @Binds
    abstract fun bindRemoveTempFileUseCase(RemoveTempFile: RemoveTempFileImpl): RemoveTempFile
}