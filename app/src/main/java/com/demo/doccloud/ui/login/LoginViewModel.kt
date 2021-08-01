package com.demo.doccloud.ui.login

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demo.doccloud.R
import com.demo.doccloud.data.repository.Repository
import com.demo.doccloud.domain.Event
import com.demo.doccloud.ui.dialogs.loading.LoadingDialogViewModel
import com.demo.doccloud.utils.GlobalUtil
import com.demo.doccloud.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: Repository,
) : ViewModel(),
    LoadingDialogViewModel {

    //fragment_login.xml use this variables to set login and password by Two-way data binding
    var login: String = ""
    var password: String = ""

    //handle Login states
    sealed class LoginState {
        object Authenticated : LoginState()
        class InvalidCredentials(val fields: List<Pair<String, Int>>) : LoginState()
        class LoginAlertDialog(val msg: String) : LoginState()
    }

    //initialize the state variable
    private val _loginState = MutableLiveData<Event<LoginState>>()
    val loginState: LiveData<Event<LoginState>>
        get() = _loginState

    fun doLoginWithGoogle(data: Intent?){
        showDialog(R.string.loading_dialog_message_login)
        viewModelScope.launch {
            val result = repository.doLoginWithGoogle(data)
            when(result.status){
                Result.Status.SUCCESS -> {
                    GlobalUtil.user = result.data
                    _loginState.value = Event(
                        LoginState.Authenticated
                    )
                }
                Result.Status.ERROR -> {
                    _loginState.value = Event(
                        LoginState.LoginAlertDialog(result.msg!!)
                    )
                }
            }
            hideDialog()
        }
    }

    //called from fragment_login.xml directly
    fun doLogin() {
        if (isValidLoginPassword(login.trim(), password.trim())) {
            //showDialog(R.string.loading_dialog_message_login)
        }
    }

    //validate login and password fields
    //only checks if login and password are empty
    private fun isValidLoginPassword(login: String, password: String): Boolean {
        val invalidFields = arrayListOf<Pair<String, Int>>()

        if (login.isEmpty()) {
            invalidFields.add(INPUT_INVALID_LOGIN)
        }

        if (password.isEmpty()) {
            invalidFields.add(INPUT_INVALID_LOGIN_PASSWORD)
        }

        if (invalidFields.isNotEmpty()) {
            _loginState.value =
                Event(
                    LoginState.InvalidCredentials(
                        invalidFields
                    )
                )
            return false
        }
        return true
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

    companion object {
        val INPUT_INVALID_LOGIN =
            "INPUT_INVALID_LOGIN" to R.string.login_input_layout_error_invalid

        val INPUT_INVALID_LOGIN_PASSWORD =
            "INPUT_INVALID_LOGIN_PASSWORD" to R.string.login_input_password_error
    }
}