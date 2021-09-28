package com.demo.doccloud.ui.register

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demo.doccloud.R
import com.demo.doccloud.domain.entities.SignUpParams
import com.demo.doccloud.domain.entities.User
import com.demo.doccloud.domain.usecases.contracts.RegisterNewUserByEmail
import com.demo.doccloud.ui.dialogs.loading.LoadingDialogViewModel
import com.demo.doccloud.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import javax.inject.Inject

private const val REGEX_PASSWORD = "^(?=.*?[a-z])(?=.*?[0-9]).{6,}$"


@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerNewUserByEmailUseCase: RegisterNewUserByEmail
) : ViewModel(), LoadingDialogViewModel {
    val signupParams = SignUpParams()
    var passwordConfirmation = ""

    sealed class RegisterStates {
        class InvalidFields(val fields: List<Pair<String, Int>>) : RegisterStates()
        class SignUpFailure(val message: String) : RegisterStates()
        class SignUpSuccess (val user: User) : RegisterStates()
    }

    private val _registerStates = MutableLiveData<Event<RegisterStates>>()
    val registerStates: LiveData<Event<RegisterStates>>
        get() = _registerStates

    fun signUp() {
        if(validateFields()){
            showDialog(R.string.loading_dialog_message_please_wait)
            viewModelScope.launch {
                try {
                    val user = registerNewUserByEmailUseCase(params = signupParams)
                    _registerStates.value = Event(
                        RegisterStates.SignUpSuccess(user)
                    )
                }catch (e: Exception){
                    _registerStates.value = Event(
                        RegisterStates.SignUpFailure(e.message.toString())
                    )
                }
                hideDialog()
            }
        }

    }

    private fun validateFields(): Boolean {
        val invalidFields = arrayListOf<Pair<String, Int>>()
        if (signupParams.name.isEmpty()) {
            invalidFields.add(INPUT_NAME)
        }
        if (signupParams.email.isEmpty()) {
            invalidFields.add(INPUT_EMAIL)
        }
        if (signupParams.password.isEmpty()) {
            invalidFields.add(INPUT_PASSWORD)
        }
        if (passwordConfirmation.isEmpty()) {
            invalidFields.add(INPUT_CONFIRM_PASSWORD)
        }

        if (!Pattern.matches(REGEX_PASSWORD, signupParams.password)) {
            invalidFields.add(INPUT_PASSWORD)
        }
        if (passwordConfirmation != signupParams.password) {
            invalidFields.add(INPUT_CONFIRM_PASSWORD)
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(signupParams.email).matches()) {
            invalidFields.add(INPUT_EMAIL)
        }
        if (invalidFields.isNotEmpty()) {
            _registerStates.value = Event(
                RegisterStates.InvalidFields(
                    invalidFields
                )
            )

            return false
        }
        return true
    }

    companion object {
        val INPUT_NAME =
            "INPUT_NAME" to R.string.signup_field_invalid_name
        val INPUT_EMAIL = "INPUT_EMAIL" to R.string.signup_field_invalid_email
        val INPUT_PASSWORD =
            "INPUT_PASSWORD" to R.string.signup_field_invalid_password
        val INPUT_CONFIRM_PASSWORD =
            "INPUT_CONFIRM_PASSWORD" to R.string.signup_field_invalid_confirm_password
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