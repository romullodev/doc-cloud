package com.demo.doccloud.ui.home

import androidx.lifecycle.*
import androidx.navigation.NavDirections
import com.demo.doccloud.R
import com.demo.doccloud.ui.dialogs.loading.LoadingDialogViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import android.net.Uri
import androidx.annotation.VisibleForTesting
import com.demo.doccloud.domain.entities.*
import com.demo.doccloud.domain.usecases.contracts.*
import com.demo.doccloud.domain.usecases.contracts.GetAllDocs
import com.demo.doccloud.utils.*
import java.io.*
import kotlin.Exception


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val copyFileUseCase: CopyFile,
    private val generateDocPdfUseCase: GenerateDocPdf,
    private val doLogoutUseCase: DoLogout,
    private val deleteDocUseCase: DeleteDoc,
    private val getUserUseCase: GetUser,
    private val scheduleToSyncDataUseCase: ScheduleToSyncData,
    private val generatePDFLink: GeneratePDFLink,
    getAllDocsUseCase: GetAllDocs
) : ViewModel(),
    LoadingDialogViewModel {

    private val _navigationCommands = MutableLiveData<Event<NavigationCommand>>()
    val navigationCommands: LiveData<Event<NavigationCommand>>
        get() = _navigationCommands

    init {
        setupInitVariables()
    }

    //this liveData is used from xml
    var docs: LiveData<List<Doc>> = getAllDocsUseCase()

    //this liveData will be used from xml
    val listDocSize: LiveData<Int> = Transformations.map(docs) { list ->
        list.size
    }

    sealed class HomeState {
        class HomeToastMessage(val msg: String) : HomeState()
        class HomeAlertDialog(val msg: Int) : HomeState()
        data class SharePdf(val data: File) : HomeState()
        data class SharePdfLink(val uri: Uri) : HomeState()
    }

    private val _homeState = MutableLiveData<Event<HomeState>>()
    val homeState: LiveData<Event<HomeState>>
        get() = _homeState

    //to help retrieve current doc select from popup menu item
    var currDoc: Doc? = null

    //handle navigation between fragments
    sealed class NavigationCommand {
        data class To(val directions: NavDirections) : NavigationCommand()
    }

    //helper method to help navigate using navigation command
    fun navigate(directions: NavDirections) {
        _navigationCommands.value = Event(NavigationCommand.To(directions))
    }

    fun sharePdfDoc() {
        showDialog(R.string.loading_dialog_message_generating_pdf)
        viewModelScope.launch {
            try {
                val pdfFile = generateDocPdfUseCase(currDoc!!)
                _homeState.value = Event(HomeState.SharePdf(pdfFile))
            }catch (e: Exception){
                Timber.d(e.printStackTrace().toString())
                _homeState.value = Event(
                    HomeState.HomeAlertDialog(
                        R.string.home_alert_error_generate_pdf
                    )
                )
            }
            hideDialog()
        }
    }

    fun sharePdfLink(){
        showDialog(R.string.loading_dialog_message_generating_pdf_link)
        viewModelScope.launch {
            try {
                val uri = generatePDFLink(currDoc!!)
                _homeState.value = Event(HomeState.SharePdfLink(uri))
            }catch (e: Exception){
                Timber.d(e.printStackTrace().toString())
                _homeState.value = Event(
                    HomeState.HomeAlertDialog(
                        R.string.home_alert_error_generate_pdf_link
                    )
                )
            }
            hideDialog()
        }
    }

    fun doLogout() {
        showDialog(R.string.loading_dialog_message_logout)
        viewModelScope.launch {
            try {
                doLogoutUseCase()
                navigate(HomeFragmentDirections.actionHomeFragmentToLoginFragment())
            }catch (e: Exception){
                Timber.i(e.toString())
            }
            hideDialog()
        }
    }

    fun deleteDoc() {
        showDialog(R.string.loading_dialog_message_please_wait)
        viewModelScope.launch {
            try {
                val msg = deleteDocUseCase(currDoc!!)
                _homeState.value = Event(
                    HomeState.HomeToastMessage(
                        msg
                    )
                )
            }catch (e: Exception){
                _homeState.value = Event(
                    HomeState.HomeAlertDialog(
                        R.string.home_toast_delete_error
                    )
                )
            }
            hideDialog()
        }
    }

    fun copyAndNavigateToCrop(uris: List<Uri?>) {
        viewModelScope.launch {
            try{
                val photos = ArrayList<Photo>()
                uris.forEach { uri ->
                    val copiedFile = copyFileUseCase(uri!!)
                    photos.add(
                        Photo(
                            id = System.currentTimeMillis(),
                            path = copiedFile!!.absolutePath
                        )
                    )
                }
                navigate(
                    HomeFragmentDirections.actionHomeFragmentToCropFragment(
                        photos = ListPhotoArg(photos),
                        root = BackToRoot(rootDestination = RootDestination.HOME_DESTINATION)
                    )
                )
            }catch (e: Exception){
                Timber.d(e.toString())
                _homeState.value = Event(
                    HomeState.HomeAlertDialog(
                        R.string.home_alert_error_copy_image_from_gallery
                    )
                )
            }
        }
    }

    //verify the user authentication when start the app
    //we're using a sessionManager object to check user authentication
    //start with the home screen instead of login screen  is a concept from google called Conditional Navigation
    //can not be private for the sake of the tests
    fun setupInitVariables() {
        viewModelScope.launch {
            try {
                val user = getUserUseCase()
                Global.user.value = Event(user)
                scheduleToSyncDataUseCase()
            }catch (e: Exception){
                Timber.d(e.toString())
                navigate(HomeFragmentDirections.actionHomeFragmentToLoginFragment())
            }
        }
    }

    //handle loading dialog to show feedback to user (this approaches does not depend on Fragments)
    private var _loadingMessage = MutableLiveData(R.string.loading_dialog_message_please_wait)
    override val loadingMessage: LiveData<Int> get() = _loadingMessage
    private var _isDialogVisible = MutableLiveData(false)
    override val isDialogVisible: LiveData<Boolean> get() = _isDialogVisible

    override fun showDialog(message: Int?) {
        _loadingMessage.value = message
        _isDialogVisible.value = true
    }

    override fun hideDialog() {
        _isDialogVisible.value = false
    }

}