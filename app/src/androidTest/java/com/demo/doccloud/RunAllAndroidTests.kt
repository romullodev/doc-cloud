package com.demo.doccloud

import com.demo.doccloud.domain.usecases.impl.GenerateDocPdfImplTest
import com.demo.doccloud.domain.usecases.impl.GeneratePDFLinkImplTest
import com.demo.doccloud.domain.usecases.impl.GetRemoveTempFileTimeImplTest
import com.demo.doccloud.domain.usecases.impl.SaveCustomIdSyncStrategyImplTest
import com.demo.doccloud.ui.camera.CameraFragmentTest
import com.demo.doccloud.ui.crop.CropFragmentTest
import com.demo.doccloud.ui.edit.EditFragmentTest
import com.demo.doccloud.ui.home.HomeFragmentTest
import com.demo.doccloud.ui.login.LoginFragmentTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.runner.RunWith
import org.junit.runners.Suite

@ExperimentalCoroutinesApi
@RunWith(Suite::class)
@Suite.SuiteClasses(
    LoginFragmentTest::class,
    HomeFragmentTest::class,
    EditFragmentTest::class,
    CropFragmentTest::class,
    CameraFragmentTest::class,
    GenerateDocPdfImplTest::class,
    GeneratePDFLinkImplTest::class,
    GetRemoveTempFileTimeImplTest::class,
    SaveCustomIdSyncStrategyImplTest::class
)
class RunAllAndroidTests