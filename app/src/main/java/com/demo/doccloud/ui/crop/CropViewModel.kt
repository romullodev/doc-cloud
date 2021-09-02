package com.demo.doccloud.ui.crop

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import com.demo.doccloud.R
import com.demo.doccloud.data.repository.Repository
import com.demo.doccloud.domain.Doc
import com.demo.doccloud.domain.DocStatus
import com.demo.doccloud.domain.Event
import com.demo.doccloud.domain.Photo
import com.demo.doccloud.ui.dialogs.loading.LoadingDialogViewModel
import com.demo.doccloud.ui.login.LoginViewModel
import com.demo.doccloud.utils.AppConstants.Companion.TIMESTAMP_FORMAT_BR
import com.demo.doccloud.utils.Global
import com.demo.doccloud.utils.Result
import com.demo.doccloud.utils.removeItem
import com.demo.doccloud.utils.updateItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@HiltViewModel
class CropViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    sealed class CropState {
        //object SaveDocNameDialog : CropState()
        class CropAlertDialog(val msg: String) : CropState()
    }

    private val _cropState = MutableLiveData<Event<CropState>>()
    val cropState: LiveData<Event<CropState>>
        get() = _cropState

    private var _listPhoto = MutableLiveData<ArrayList<Photo>>()
    val listPhoto get() = _listPhoto

    //help to track current photo being cropped
    private var currCroppedPosition = -1

    //handle navigation between fragments
    sealed class NavigationCommand {
        object ToRoot : NavigationCommand()
    }

    //retrieve the same list reference from the previous screen
    fun setListPhoto(list: ArrayList<Photo>) {
        this._listPhoto.value = list
    }

    //save documentation locally and schedule to send to the serve via workManager when connection is available
    fun saveDocs(docName: String) {
        //showDialog(R.string.loading_dialog_message_please_wait)
        viewModelScope.launch {
            val result = repository.saveDoc(
                Doc(
                    remoteId = System.currentTimeMillis(),
                    name = docName,
                    date = SimpleDateFormat(TIMESTAMP_FORMAT_BR, Locale.US).format(System.currentTimeMillis()),
                    pages = listPhoto.value!!,
                    status = DocStatus.NOT_SENT
                )
            )
            when (result.status) {
                Result.Status.SUCCESS -> {
                    navigateToRoot()
                }
                Result.Status.ERROR -> {
                    _cropState.value = Event(
                        CropState.CropAlertDialog(result.msg!!)
                    )
                }
            }
            //hideDialog()
        }
    }

    fun addPhotos(localId: Long?){
        //showDialog(R.string.loading_dialog_message_please_wait)
        viewModelScope.launch {
            repository.addPhotos(listPhoto.value!!, localId!!)
            _navigationCommands.value = Event(NavigationCommand.ToRoot)
            //hideDialog()
        }
    }

    private val _navigationCommands = MutableLiveData<Event<NavigationCommand>>()
    val navigationCommands: LiveData<Event<NavigationCommand>>
        get() = _navigationCommands

    //helper method to help navigate using navigation command
    private fun navigateToRoot() {
        _navigationCommands.value = Event(NavigationCommand.ToRoot)
    }

    fun setCurrCroppedPosition(pos: Int) {
        this.currCroppedPosition = pos
    }

    fun removePhoto(photo: Photo) {
        File(photo.path).delete()
        this.listPhoto.removeItem(photo)
    }

//    //called from xml
//    fun saveDocName() {
//        _cropState.value = Event(CropState.SaveDocNameDialog)
//    }

    fun saveCropPhoto(uri: Uri, context: Context) {
        viewModelScope.launch {
            try {
                val newPath = copyNewFileDeleteOldOne(uri.path, context)
                _listPhoto.updateItem(
                    Photo(
                        id = _listPhoto.value!![currCroppedPosition].id,
                        path = newPath
                    ), currCroppedPosition
                )
            } catch (e: Exception) {
                Timber.e("an error occurred saving cropped image. Details:\n $e")
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
            val oldFileOnFilesDir = File(_listPhoto.value!![currCroppedPosition].path)
            //delete old file from Files Dir
            oldFileOnFilesDir.delete()
            return@withContext newFileOnFilesDir.absolutePath
        }
    }

}