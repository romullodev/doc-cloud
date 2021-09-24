package com.demo.doccloud.ui.dialogs.signup

import android.app.Dialog
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.demo.doccloud.R
import com.demo.doccloud.databinding.SignupDialogBinding
import com.demo.doccloud.domain.entities.SignUpParams
import com.demo.doccloud.utils.errorDismiss
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpDialog : DialogFragment() {

    private val signUpViewModel: SignUpViewModel by viewModels()

    //fields to validate
    private lateinit var validationFields: Map<String, TextInputLayout>

    private var _binding: SignupDialogBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val DIALOG_SIGN_UP_INTERFACE_KEY = "dialog.doc.interface.key"
        internal fun newInstance(
            listener: SignUpDialogListener,
        ) = SignUpDialog().apply {
            arguments = Bundle().apply {
                putParcelable(DIALOG_SIGN_UP_INTERFACE_KEY, listener)
            }
        }
    }

    interface SignUpDialogListener : Parcelable {
        fun onSignUpClick(params: SignUpParams, dialog: DialogFragment)
        override fun describeContents(): Int = 0
        override fun writeToParcel(dest: Parcel, flags: Int) { /* nop */ }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val listener : SignUpDialogListener = arguments?.getParcelable(DIALOG_SIGN_UP_INTERFACE_KEY)!!
        return activity?.let {
            val builder =
                MaterialAlertDialogBuilder(it, R.style.ThemeOverlay_App_MaterialAlertDialog)
            val layoutInflater = LayoutInflater.from(context)
            _binding = SignupDialogBinding.inflate(layoutInflater, null, false)

            validationFields = initValidationFields()
            setupBindings()
            setupObservers()
            setupClearErrorFields()
            setupListeners()

            val localDialog = builder.create()
            localDialog.setView(binding.root)
            localDialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun initValidationFields(): Map<String, TextInputLayout> {
        return mapOf(
            SignUpViewModel.INPUT_NAME.first to binding.content.inputLayoutName,
            SignUpViewModel.INPUT_EMAIL.first to binding.content.inputLayoutEmail,
            SignUpViewModel.INPUT_PASSWORD.first to binding.content.inputLayoutPassword,
            SignUpViewModel.INPUT_CONFIRM_PASSWORD.first to binding.content.inputLayoutPasswordAgain,
        )
    }

    private fun setupListeners() {
        binding.content.buttonLoginSignIn.setOnClickListener {
            signUpViewModel.signUp()
        }
        binding.btnClose.setOnClickListener {
            dialog?.dismiss()
        }
    }

    private fun setupClearErrorFields() {
        binding.content.inputName.addTextChangedListener {
            binding.content.inputLayoutName.errorDismiss()
        }
        binding.content.inputEmail.addTextChangedListener {
            binding.content.inputLayoutEmail.errorDismiss()
        }
        binding.content.inputPassword.addTextChangedListener {
            binding.content.inputLayoutPassword.errorDismiss()
        }
        binding.content.inputPasswordAgain.addTextChangedListener {
            binding.content.inputLayoutPasswordAgain.errorDismiss()
        }
    }

//    private fun initValidationFields() = mapOf(
//        SignUpViewModel.INPUT_NAME.first to binding.inputLayoutName,
//        SignUpViewModel.INPUT_EMAIL.first to binding.inputLayoutEmail,
//        SignUpViewModel.INPUT_PASSWORD.first to binding.inputLayoutPassword,
//        SignUpViewModel.INPUT_CONFIRM_PASSWORD.first to binding.inputLayoutPasswordAgain,
//    )

    private fun setupObservers() {
        signUpViewModel.signUpStates.observe(this){
            it.getContentIfNotHandled()?.let { state ->
                when(state){
                    is SignUpViewModel.SignUpStates.InvalidFields -> {
                        state.fields.forEach { fieldError ->
                            validationFields[fieldError.first]?.error = getString(fieldError.second)
                        }
                    }
                    is SignUpViewModel.SignUpStates.SignUpFailure -> {
                        TODO()
                    }
                }
            }
        }
    }

    private fun setupBindings() {
        binding.viewModel = signUpViewModel
        binding.lifecycleOwner = this
    }

}