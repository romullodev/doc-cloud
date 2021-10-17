package com.demo.doccloud.ui.forgot

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demo.doccloud.R
import com.demo.doccloud.domain.usecases.contracts.RecoverPassword
import com.demo.doccloud.ui.dialogs.loading.LoadingDialogViewModel
import com.demo.doccloud.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotViewModel @Inject constructor(
    private val recoverPasswordUseCase: RecoverPassword
) : ViewModel(), LoadingDialogViewModel {

    var email = ""

    sealed class ForgotStates {
        object InvalidEmail : ForgotStates()
        class RecoverFailure(val message: String) : ForgotStates()
        object RecoverSuccess : ForgotStates()
    }

    private val _forgotStates = MutableLiveData<Event<ForgotStates>>()
    val forgotStates: LiveData<Event<ForgotStates>>
        get() = _forgotStates

    fun recoverPassword() {
        if (email.trim() != "" && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showDialog(R.string.loading_dialog_message_please_wait)
            viewModelScope.launch {
                try {
                    //delay(2000)
                    recoverPasswordUseCase(email.trim())
                    _forgotStates.value = Event(ForgotStates.RecoverSuccess)
                } catch (e: Exception) {
                    _forgotStates.value = Event(ForgotStates.RecoverFailure(e.message!!))
                }
            }
        } else {
            _forgotStates.value = Event(ForgotStates.InvalidEmail)
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

