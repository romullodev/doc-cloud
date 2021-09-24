package com.demo.doccloud.ui.crop

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demo.doccloud.R
import com.demo.doccloud.domain.entities.Doc
import com.demo.doccloud.domain.entities.DocStatus
import com.demo.doccloud.domain.entities.Photo
import com.demo.doccloud.domain.usecases.contracts.AddPhotos
import com.demo.doccloud.domain.usecases.contracts.CopyFile
import com.demo.doccloud.domain.usecases.contracts.SaveDoc
import com.demo.doccloud.utils.AppConstants.Companion.TIMESTAMP_FORMAT_BR
import com.demo.doccloud.utils.Event
import com.demo.doccloud.utils.removeItem
import com.demo.doccloud.utils.updateItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CropViewModel @Inject constructor(
    private val saveDocUseCase: SaveDoc,
    private val addPhotosUseCase: AddPhotos,//in case on editFragment
    private val copyFileUseCase: CopyFile
) : ViewModel() {

    sealed class CropState {
        //object SaveDocNameDialog : CropState()
        class CropAlertDialog(val msg: Int) : CropState()
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
        viewModelScope.launch {
            try {
                saveDocUseCase(
                    Doc(
                        remoteId = System.currentTimeMillis(),
                        name = docName,
                        date = SimpleDateFormat(TIMESTAMP_FORMAT_BR, Locale.US).format(System.currentTimeMillis()),
                        pages = listPhoto.value!!,
                        status = DocStatus.NOT_SENT
                    )
                )
                navigateToRoot()
            }catch (e: Exception){
                _cropState.value = Event(
                    CropState.CropAlertDialog(
                        R.string.common_unknown_error
                    )
                )
            }
        }
    }

    fun addPhotos(localId: Long?){
        viewModelScope.launch {
            try {
                addPhotosUseCase(localId!!, listPhoto.value!!)
                _navigationCommands.value = Event(NavigationCommand.ToRoot)
            }catch (e: Exception){
                _cropState.value = Event(
                    CropState.CropAlertDialog(
                        R.string.common_unknown_error
                    )
                )
            }
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
    fun deleteAllPhotos(){
        this.listPhoto.value?.forEach {
            File(it.path).delete()
        }
    }

    fun saveCropPhoto(uri: Uri) {
        viewModelScope.launch {
            try {
                val newFile = copyFileUseCase(uri)
                _listPhoto.updateItem(
                    Photo(
                        id = _listPhoto.value!![currCroppedPosition].id,
                        path = newFile?.path!!
                    ), currCroppedPosition
                )
            } catch (e: Exception) {
                _cropState.value = Event(
                    CropState.CropAlertDialog(
                        R.string.crop_screen_error_on_save_crop
                    )
                )
            }
        }
    }

}