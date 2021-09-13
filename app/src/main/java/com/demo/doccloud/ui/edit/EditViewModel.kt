package com.demo.doccloud.ui.edit

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import com.demo.doccloud.R
import com.demo.doccloud.domain.entities.Doc
import com.demo.doccloud.domain.entities.Photo
import com.demo.doccloud.domain.usecases.contracts.*
import com.demo.doccloud.ui.dialogs.loading.LoadingDialogViewModel
import com.demo.doccloud.utils.BackToRoot
import com.demo.doccloud.utils.Event
import com.demo.doccloud.utils.ListPhotoArg
import com.demo.doccloud.utils.RootDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@HiltViewModel
class EditViewModel @Inject constructor(
    private val copyFileUseCase: CopyFile,
    private val generateDocPdfUseCase: GenerateDocPdf,
    private val getDocByIdUseCase: GetDocById,
    private val updatedDocNameUseCase: UpdatedDocName,
    private val deleteDocPhotoUseCase: DeleteDocPhoto,
    private val updateDocPhotoUseCase: UpdateDocPhoto,
) : ViewModel(),
    LoadingDialogViewModel {

    sealed class EditState {
        data class SharePdf(val data: File): EditState()
    }

    private val _editState = MutableLiveData<Event<EditState>>()
    val editState: LiveData<Event<EditState>>
        get() = _editState


    private var _doc = MutableLiveData<Doc>()
    val doc get() = _doc

    //private val editableDoc : EditableDoc = EditableDoc()
    //this will help to track the select photo on EditFragment, since it is assigned before navigate to EditCropFragment
    private var _selectedPhoto = MutableLiveData<Photo>()
    val selectedPhoto get() = _selectedPhoto

    //handle navigation between fragments
    sealed class NavigationCommand {
        data class To(val directions: NavDirections) : NavigationCommand()
    }

    fun setSelectedPhoto(photo: Photo) {
        _selectedPhoto.value = photo
    }

    fun updateNameDoc(localId: Long, remoteId: Long, newName: String) {
        viewModelScope.launch {
            updatedDocNameUseCase(localId = localId, remoteId = remoteId, newName)
        }
    }

    //retrieve the same list reference from the previous screen
    fun getDocById(id: Long) {
        viewModelScope.launch {
            try {
                val doc = getDocByIdUseCase(id)
                _doc.value = doc
            }catch (e: Exception){
                Timber.d(e.toString())
            }
        }
    }

    fun shareDoc(){
        showDialog(R.string.loading_dialog_message_generating_pdf)
        viewModelScope.launch {
            try {
                val pdfFile = generateDocPdfUseCase(_doc.value!!)
                _editState.value = Event(EditState.SharePdf(pdfFile))
            }catch (e: Exception){
                Timber.d("failure on  generated pdf. \nDetails: $e")
            }
            hideDialog()
        }
    }

    //https://stackoverflow.com/questions/57093479/get-real-path-from-uri-data-is-deprecated-in-android-q
    //same function define on HomeViewModel
    fun copyAndNavigateToCrop(context: Context, uris: List<Uri?>) {
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
                    EditFragmentDirections.actionGlobalCropFragment(
                        photos = ListPhotoArg(photos),
                        root = BackToRoot(
                            rootDestination = RootDestination.EDIT_DESTINATION,
                            localId = doc.value?.localId
                        ),
                    )
                )
            }catch (e:Exception){
                Timber.i(e.toString())
            }
        }
    }

    /**
     * these methods above are called from EditFragment and these ones bellow are called from EditCropFragment
     */

    fun deleteSelectedDocPhoto() {
        viewModelScope.launch {
            try {
                deleteDocPhotoUseCase(localId = doc.value?.localId!!, photo = selectedPhoto.value!!)
            }catch (e: Exception){
                Timber.d(e.toString())
            }
        }
    }

    fun getSelectedPhoto() = selectedPhoto.value

    private val _navigationCommands = MutableLiveData<Event<NavigationCommand>>()
    val navigationCommands: LiveData<Event<NavigationCommand>>
        get() = _navigationCommands

    //helper method to help navigate using navigation command
    fun navigate(directions: NavDirections) {
        _navigationCommands.value = Event(NavigationCommand.To(directions))
    }

    fun updateDocPhoto(uri: Uri) {
        viewModelScope.launch {
            try {
                val newFile = copyFileUseCase(uri)
                _selectedPhoto.value = _selectedPhoto.value?.copy(path = newFile?.path!!)
                updateDocPhotoUseCase(
                    localId = doc.value?.localId!!,
                    photo = selectedPhoto.value!!
                )
            }catch (e: Exception){
                Timber.e("an error updateDocPhoto. Details:\n $e")
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