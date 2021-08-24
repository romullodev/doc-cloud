package com.demo.doccloud.ui.crop

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import com.demo.doccloud.data.repository.Repository
import com.demo.doccloud.domain.Event
import com.demo.doccloud.domain.Photo
import com.demo.doccloud.utils.Global
import com.demo.doccloud.utils.removeItem
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
class CropViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    sealed class CropState {
        object SaveDocNameDialog: CropState()
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
        data class To(val directions: NavDirections) : NavigationCommand()
    }

    private val _navigationCommands = MutableLiveData<Event<NavigationCommand>>()
    val navigationCommands: LiveData<Event<NavigationCommand>>
        get() = _navigationCommands

    //helper method to help navigate using navigation command
    fun navigate(directions: NavDirections) {
        _navigationCommands.value = Event(NavigationCommand.To(directions))
    }

    fun setCurrCroppedPosition(pos: Int) {
        this.currCroppedPosition = pos
    }

    fun removePhoto(photo: Photo) {
        File(photo.path).delete()
        this.listPhoto.removeItem(photo)
    }

    fun saveDocName() {
        _cropState.value = Event(CropState.SaveDocNameDialog)
    }

    fun saveCropPhoto(uri: Uri, context: Context) {
        viewModelScope.launch {
            try{
                val newPath = copyNewFileDeleteOldOne(uri.path, context)
                _listPhoto.updateItem(
                    Photo(
                        id = _listPhoto.value!![currCroppedPosition].id,
                        path = newPath
                    ), currCroppedPosition
                )
            }catch (e: Exception){
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

    //retrieve the same list reference from the previous screen
    fun setListPhoto(list: ArrayList<Photo>) {
        this._listPhoto.value = list
    }

}