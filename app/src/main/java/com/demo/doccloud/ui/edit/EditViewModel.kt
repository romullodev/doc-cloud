package com.demo.doccloud.ui.edit

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import com.demo.doccloud.R
import com.demo.doccloud.data.repository.Repository
import com.demo.doccloud.domain.Doc
import com.demo.doccloud.domain.Event
import com.demo.doccloud.domain.Photo
import com.demo.doccloud.ui.dialogs.loading.LoadingDialogViewModel
import com.demo.doccloud.ui.home.HomeViewModel
import com.demo.doccloud.utils.Global
import com.demo.doccloud.utils.Result
import com.demo.doccloud.utils.addWithDiffId
import com.demo.doccloud.utils.updateItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class EditViewModel @Inject constructor(
    private val repository: Repository
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
            repository.updateDocName(localId = localId, remoteId = remoteId, newName)
        }
    }

    //retrieve the same list reference from the previous screen
    fun getDocById(id: Long) {
        viewModelScope.launch {
            val result = repository.getDoc(id)
            when (result.status) {
                Result.Status.SUCCESS -> {
                    _doc.value = result.data!!
                }
                Result.Status.ERROR -> {
                    TODO()
                }
            }
        }
    }

    fun shareDoc(){
        showDialog(R.string.loading_dialog_message_generating_pdf)
        viewModelScope.launch {
            val result = repository.generatePdf(_doc.value!!)
            when(result.status){
                Result.Status.SUCCESS -> {
                    _editState.value = Event(EditState.SharePdf(result.data!!))
                    Timber.d("pdf generated on ${result.data.path}")
                }
                Result.Status.ERROR -> {
                    Timber.d("failure on  generated pdf. \nDetails: ${result.msg}")
                }
            }
            hideDialog()
        }
    }

    /**
     * these methods above are called from EditFragment and these ones bellow are called from EditCropFragment
     */

    fun deleteSelectedDocPhoto() {
        viewModelScope.launch {
            repository.deleteDocPhoto(
                localId = doc.value?.localId!!,
                remoteId = doc.value?.remoteId!!,
                photo = selectedPhoto.value!!
            )
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

    fun updateDocPhoto(uri: Uri, context: Context) {
        viewModelScope.launch {
            try {
                val newPath = copyNewFileDeleteOldOne(uri.path, context)
                _selectedPhoto.value = _selectedPhoto.value?.copy(path = newPath)
                repository.updateDocPhotos(
                    localId = doc.value?.localId!!,
                    remoteId = doc.value?.remoteId!!,
                    photo = selectedPhoto.value!!
                )
            } catch (e: Exception) {
                Timber.e("an error updateDocPhoto. Details:\n $e")
            }
        }
    }

    //copy a file from cache dir (and delete) to files Dir
    private suspend fun copyNewFileDeleteOldOne(croppedUriPath: String?, context: Context): String {
        return withContext(Dispatchers.IO) {
            val fileOnCacheDir = File(croppedUriPath!!)
            //this is the file that we want
            val newFileOnFilesDir =
                File(Global.getOutputDirectory(context), fileOnCacheDir.name)
            //copy file from cache Dir
            fileOnCacheDir.copyTo(newFileOnFilesDir, true)
            //delete file from cache Dir
            fileOnCacheDir.delete()
            //val oldFileOnFilesDir = File(_uriPhotos.value?.get(position)?.uriPath!!)
            val oldFileOnFilesDir = File(selectedPhoto.value?.path!!)
            //delete old file from Files Dir
            oldFileOnFilesDir.delete()
            return@withContext newFileOnFilesDir.absolutePath
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