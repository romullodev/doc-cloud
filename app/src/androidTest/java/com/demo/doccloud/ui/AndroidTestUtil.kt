package com.demo.doccloud.ui

import android.content.Context
import com.demo.doccloud.data.repository.Repository
import com.demo.doccloud.domain.entities.Doc
import com.demo.doccloud.domain.entities.DocStatus
import com.demo.doccloud.domain.entities.Photo
import com.demo.doccloud.domain.entities.User
import com.demo.doccloud.domain.usecases.contracts.GenerateDocPdf
import com.demo.doccloud.domain.usecases.impl.*
import com.demo.doccloud.ui.home.HomeViewModel
import kotlinx.coroutines.Dispatchers

class AndroidTestUtil {
    companion object{
        fun getUser() = User("any","any")

        fun getRealDoc(context: Context) = Doc(
            remoteId = 1L,
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

        fun getHomeViewModelWithMockGeneratePdfUseCase(
            context: Context,
            repository: Repository,
            generateDocPdf: GenerateDocPdf
        ): HomeViewModel{
            val copyFileUseCase = CopyFileImpl(context, Dispatchers.Default)
            val doLogoutUseCase = DoLogoutImpl(repository)
            val scheduleToDeleteRemoteDoc = ScheduleToDeleteRemoteDocImpl(context)
            val deleteLocalDoc = DeleteLocalDocImpl(repository)
            val deleteDocUseCase = DeleteDocImpl(deleteLocalDoc, scheduleToDeleteRemoteDoc, context)
            val getUserUseCase = GetUserImpl(repository)
            val scheduleToSyncData = ScheduleToSyncDataImpl(context)
            val getAllDocsUse = GetAllDocsImpl(repository)

            return HomeViewModel(
                copyFileUseCase,
                generateDocPdf,
                doLogoutUseCase,
                deleteDocUseCase,
                getUserUseCase,
                scheduleToSyncData,
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
            val getAllDocsUse = GetAllDocsImpl(repository)

            return HomeViewModel(
                copyFileUseCase,
                generateDocPdfUseCase,
                doLogoutUseCase,
                deleteDocUseCase,
                getUserUseCase,
                scheduleToSyncData,
                getAllDocsUse
            )
        }
    }
}