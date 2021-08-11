package com.demo.doccloud.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import com.demo.doccloud.R
import com.demo.doccloud.data.repository.Repository
import com.demo.doccloud.domain.Event
import com.demo.doccloud.ui.dialogs.loading.LoadingDialogViewModel
import com.demo.doccloud.ui.login.LoginViewModel
import com.demo.doccloud.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel(),
    LoadingDialogViewModel {

    sealed class HomeState {
        //class SearchListResultState(val result: List<Load>) : HomeState()
        class HomeAlertDialog(val msg: String) : HomeState()
    }

    private val _homeState = MutableLiveData<Event<HomeState>>()
    val homeState: LiveData<Event<HomeState>>
        get() = _homeState


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

    fun doLogout(){
        showDialog(R.string.loading_dialog_message_logout)
        viewModelScope.launch {
            val result = repository.doLogout()
            when (result.status) {
                Result.Status.SUCCESS -> {
                    navigate(HomeFragmentDirections.actionHomeFragmentToLoginFragment())
                }
                Result.Status.ERROR -> {
                    Timber.i(result.msg)
                }
            }
            hideDialog()
        }
    }


    //verify the user authentication when start the app
    //we're using a sessionManager object to check user authentication
    //start with the home screen instead of login screen  is a concept from google called Conditional Navigation
    private fun setupInitVariables() {
        viewModelScope.launch {
            val result = repository.getUser()
            when (result.status) {
                Result.Status.SUCCESS -> {
                    Timber.i("User authenticated")
                }
                Result.Status.ERROR -> {
                    //Timber.i(result.exception)
                    navigate(HomeFragmentDirections.actionHomeFragmentToLoginFragment())
                }
            }
        }
    }

    init {
        setupInitVariables()
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