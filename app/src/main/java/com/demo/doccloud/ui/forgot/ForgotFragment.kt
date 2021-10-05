package com.demo.doccloud.ui.forgot

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.demo.doccloud.R
import com.demo.doccloud.databinding.ForgotFragmentBinding
import com.demo.doccloud.utils.errorDismiss
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ForgotFragment : DialogFragment() {

    private val forgotViewModel: ForgotViewModel by viewModels()

    private var _binding: ForgotFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder =
                MaterialAlertDialogBuilder(it, R.style.ThemeOverlay_App_MaterialAlertDialog)
            val layoutInflater = LayoutInflater.from(context)
            _binding = ForgotFragmentBinding.inflate(layoutInflater, null, false)

            setupBindings()
            setupListeners()
            setupObserver()
            setupClearErrorField()

            val localDialog = builder.create()
            localDialog.setView(binding.root)
            localDialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun setupClearErrorField() {
        binding.content.inputEmail.addTextChangedListener {
            binding.content.inputLayoutEmail.errorDismiss()
        }
    }

    private fun setupObserver() {
        forgotViewModel.forgotStates.observe(this){
            it.getContentIfNotHandled()?.let {state->
                when(state){
                    is ForgotViewModel.ForgotStates.InvalidEmail -> {
                        binding.content.inputLayoutEmail.error = getString(R.string.common_field_invalid_email)
                    }
                    is ForgotViewModel.ForgotStates.RecoverFailure -> {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    }
                    is ForgotViewModel.ForgotStates.RecoverSuccess -> {
                        Toast.makeText(requireContext(), getString(R.string.forgot_check_your_email), Toast.LENGTH_LONG).show()
                        findNavController().popBackStack()
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.content.buttonLoginSignIn.setOnClickListener {
            forgotViewModel.recoverPassword()
        }
        binding.btnClose.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupBindings() {
        binding.forgotViewModel = forgotViewModel
        binding.lifecycleOwner = this
    }

}