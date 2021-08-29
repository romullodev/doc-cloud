package com.demo.doccloud.ui.edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import com.demo.doccloud.data.repository.Repository
import com.demo.doccloud.domain.Doc
import com.demo.doccloud.domain.Event
import com.demo.doccloud.domain.Photo
import com.demo.doccloud.ui.home.HomeViewModel
import com.demo.doccloud.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel(){

    private var _doc = MutableLiveData<Doc>()
    val doc get() = _doc
    private val editableDoc : EditableDoc = EditableDoc()
    //this will help to track the select photo on EditFragment
    var selectedPhoto: Photo? = null

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

    fun setNewNameDoc(newName: String?){
        editableDoc.name = newName
    }
    fun setNewPhotos(photos: MutableList<Photo>?){
        editableDoc.photos = photos
    }


    //retrieve the same list reference from the previous screen
    fun getDocById(id: Long) {
        viewModelScope.launch {
            val result = repository.getDoc(id)
            when(result.status){
                Result.Status.SUCCESS -> {
                    _doc.value = result.data!!
                }
                Result.Status.ERROR -> {
                    TODO()
                }
            }
        }
    }

    private data class EditableDoc(
        var name: String? = null,
        var photos: MutableList<Photo>? = null
    )
}