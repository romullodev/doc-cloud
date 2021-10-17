package com.demo.doccloud.di

import com.demo.doccloud.domain.di.UseCaseForWorkManagerModule
import com.demo.doccloud.domain.di.UseCaseModule
import com.demo.doccloud.domain.usecases.contracts.*
import com.demo.doccloud.domain.usecases.impl.*
import dagger.Binds
import dagger.Module
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
//
//@TestInstallIn(
//    components = [SingletonComponent::class],
//    replaces = [UseCaseForWorkManagerModule::class]
//)
//@Module
//abstract class FakeUseCaseForWorkManagerModule{
//    @Binds
//    abstract fun bindGetDocByIdUseCase(getDocById: GetDocByIdImpl): GetDocById
//
//    @Binds
//    abstract fun bindUpdateLocalDocUseCase(updateLocalDoc: UpdateLocalDocImpl): UpdateLocalDoc
//
//    @Binds
//    abstract fun bindAddPhotoToRemoteDocUseCase(addPhotoToRemoteDoc: AddPhotoToRemoteDocImpl): AddPhotoToRemoteDoc
//
//    @Binds
//    abstract fun bindDeleteRemoteDocPhotoUseCase(deleteRemoteDocPhoto: DeleteRemoteDocPhotoImpl): DeleteRemoteDocPhoto
//
//    @Binds
//    abstract fun bindDeleteRemoteDocUseCase(deleteRemoteDoc: DeleteRemoteDocImpl): DeleteRemoteDoc
//
//    @Binds
//    abstract fun bindGetSavedCustomIdSyncStrategyUseCase(getSavedCustomIdSyncStrategy: GetSavedCustomIdSyncStrategyImpl): GetSavedCustomIdSyncStrategy
//
//    @Binds
//    abstract fun bindGetGetSyncStrategyUseCase(getSyncStrategy: GetSyncStrategyImpl): GetSyncStrategy
//
//    @Binds
//    abstract fun bindSyncDataUseCase(syncData: SyncDataImpl): SyncData
//
//    @Binds
//    abstract fun bindSaveCustomIdSyncStrategyUseCase(saveCustomIdSyncStrategy: SaveCustomIdSyncStrategyImpl): SaveCustomIdSyncStrategy
//
//    @Binds
//    abstract fun bindUpdateRemoteDocNameUseCase(updateRemoteDocName: UpdateRemoteDocNameImpl): UpdateRemoteDocName
//
//    @Binds
//    abstract fun bindUpdateRemoteDocPhotoUseCase(updateRemoteDocPhoto: UpdateRemoteDocPhotoImpl): UpdateRemoteDocPhoto
//
//    @Binds
//    abstract fun bindUploadDocUseCase(uploadDoc: UploadDocImpl): UploadDoc
//}
