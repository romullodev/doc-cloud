package com.demo.doccloud.ui.camera

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import com.demo.doccloud.utils.Event
import com.demo.doccloud.domain.entities.Photo
import com.demo.doccloud.utils.addNewItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor() : ViewModel() {

    private val _listThumbnail = MutableLiveData<ArrayList<Photo>>()
    val listThumbnail: LiveData<ArrayList<Photo>>
        get() = _listThumbnail

    //when a photo is capture (called from code)
    fun addItem(photo: Photo) {
        _listThumbnail.addNewItem(photo)
    }

    fun deleteAllItem() {
        listThumbnail.value?.let { list ->
            if (list.isNotEmpty()) {
                viewModelScope.launch(Dispatchers.IO) {
                    list.forEach {
                        File(it.path).delete()
                    }
                }
            }
        }
    }

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

}