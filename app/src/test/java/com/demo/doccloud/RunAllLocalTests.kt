package com.demo.doccloud

import com.demo.doccloud.domain.usecases.impl.*
import com.demo.doccloud.ui.camera.CameraViewModelTest
import com.demo.doccloud.ui.crop.CropViewModelTest
import com.demo.doccloud.ui.edit.EditViewModelTest
import com.demo.doccloud.ui.home.HomeViewModelTest
import com.demo.doccloud.ui.login.LoginViewModelTest
import com.demo.doccloud.workers.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.runner.RunWith
import org.junit.runners.Suite

@ExperimentalCoroutinesApi
@RunWith(Suite::class)
@Suite.SuiteClasses(
    //ViewModels
    LoginViewModelTest::class,
    HomeViewModelTest::class,
    EditViewModelTest::class,
    CropViewModelTest::class,
    CameraViewModelTest::class,
    //Use cases
    AddPhotosToLocalDocImplTest::class,
    AddPhotoToRemoteDocImplTest::class,
    CopyFileImplTest::class,
    DeleteLocalDocPhotoImplTest::class,
    DoLogoutImplTest::class,
    GetSavedCustomIdSyncStrategyImplTest::class,
    GetSyncStrategyImplTest::class,
    RecoverPasswordImplTest::class,
    SyncDataImplTest::class,
    UpdateLocalDocImplTest::class,
    UpdateLocalDocNameImplTest::class,
    UpdateLocalDocPhotoImplTest::class,
    //workers
    AddDocPhotosWorkerTest::class,
    DeleteDocPageWorkerTest::class,
    DeleteDocWorkerTest::class,
    RemoveTempFileWorkerTest::class,
    SyncDataWorkerTest::class,
    UpdateDocNameWorkerTest::class,
    UpdateDocPageWorkerTest::class,
    UploadDocWorkerTest::class
)

class RunAllLocalTests