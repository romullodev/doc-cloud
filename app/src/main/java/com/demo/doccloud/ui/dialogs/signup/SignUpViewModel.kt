package com.demo.doccloud.ui.dialogs.signup

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.demo.doccloud.R
import com.demo.doccloud.domain.entities.SignUpParams
import com.demo.doccloud.ui.login.LoginViewModel
import com.demo.doccloud.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.regex.Pattern
import javax.inject.Inject

private const val REGEX_PASSWORD = "^(?=.*?[a-z])(?=.*?[0-9]).{6,}$"


@HiltViewModel
class SignUpViewModel @Inject constructor() : ViewModel() {
    val signupParams = SignUpParams()
    var passwordConfirmation = ""

    sealed class SignUpStates {
        class InvalidFields(val fields: List<Pair<String, Int>>) : SignUpStates()
        class SignUpFailure(val message: String) : SignUpStates()
    }

    private val _signUpStates = MutableLiveData<Event<SignUpStates>>()
    val signUpStates: LiveData<Event<SignUpStates>>
        get() = _signUpStates

    fun signUp() {
        if(validateFields()){
            //do something
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
            _signUpStates.value = Event(
                SignUpStates.InvalidFields(
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

}