package com.demo.doccloud.ui.login

import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.fragment.findNavController
import com.demo.doccloud.R
import com.demo.doccloud.databinding.FragmentLoginBinding
import com.demo.doccloud.domain.entities.User
import com.demo.doccloud.ui.dialogs.alert.AppAlertDialog
import com.demo.doccloud.utils.AppConstants.Companion.USER_SIGN_UP_KEY
import com.demo.doccloud.utils.DialogsHelper
import com.demo.doccloud.utils.Global
import com.demo.doccloud.utils.errorDismiss
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment() : Fragment(), AppAlertDialog.DialogMaterialListener {
    private lateinit var loginGoogleLauncher: ActivityResultLauncher<Intent>

    //help validate the credentials
    private lateinit var validationFields: Map<String, TextInputLayout>

    private val loginViewModel: LoginViewModel by viewModels()

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupGoogleLogin()
        setupBindingVariable()
        setupObservers()
        setupListeners()
        validationFields = initValidationFields()
        //finishes the activity when physical back button is pressed
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            activity?.finish()
        }
    }

    private fun setupGoogleLogin() {
        loginGoogleLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            loginViewModel.doLoginWithGoogle(it.data)
        }
    }

    private fun setupBindingVariable() {
        binding.loginViewModel = loginViewModel
        //require to kill binding objects when this fragment is destroyed (lifecycle aware)
        binding.lifecycleOwner = viewLifecycleOwner
    }

    private fun setupListeners() {
        //here, we're using an extension function from TextView class
        binding.inputLoginEmail.addTextChangedListener {
            //here, we're using our own extension function
            binding.inputLayoutLogin.errorDismiss()
        }

        binding.inputLoginPassword.addTextChangedListener {
            binding.inputLayoutLoginPassword.errorDismiss()
        }

        binding.buttonLoginSignInGoogle.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
            loginGoogleLauncher.launch(googleSignInClient.signInIntent)
        }

        binding.textViewLoginRegistration.setOnClickListener {
            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToRegisterUser())
        }
    }

    private fun setupObservers() {
        loginViewModel.loginState.observe(viewLifecycleOwner, {
            // Only proceed if the event has never been handled
            it.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is LoginViewModel.LoginState.Authenticated -> {
                        findNavController().popBackStack()
                    }
                    //notify in case of empty fields
                    is LoginViewModel.LoginState.InvalidCredentials -> {
                        state.fields.forEach { fieldError ->
                            validationFields[fieldError.first]?.error =
                                getString(fieldError.second)
                        }
                    }
                    is LoginViewModel.LoginState.LoginAlertDialog -> {
                        DialogsHelper.showAlertDialog(
                            DialogsHelper.getInfoAlertParams(
                                msg = state.msg
                            ),
                            this,
                            requireActivity(),
                        )
                    }
                }
            }
        })

        findNavController().run {
            val navBackStackEntry = getBackStackEntry(R.id.loginFragment)
            val savedStateHandle = navBackStackEntry.savedStateHandle
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME && savedStateHandle.contains(USER_SIGN_UP_KEY)) {
                    val user = savedStateHandle.get<User>(USER_SIGN_UP_KEY)
                    loginViewModel.processRegisterUser(user)
                }
            }

            navBackStackEntry.lifecycle.addObserver(observer)

            viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_DESTROY) {
                    navBackStackEntry.lifecycle.removeObserver(observer)
                }
            })
        }
    }

    //helper method to initialize validation fields (for empty fields)
    private fun initValidationFields() = mapOf(
        LoginViewModel.INPUT_INVALID_LOGIN.first to binding.inputLayoutLogin,
        LoginViewModel.INPUT_INVALID_LOGIN_PASSWORD.first to binding.inputLayoutLoginPassword
    )

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        dialog.dismiss()
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        dialog.dismiss()
    }

    override fun describeContents() = 0
    constructor(parcel: Parcel) : this()
    companion object CREATOR : Parcelable.Creator<LoginFragment> {
        override fun createFromParcel(parcel: Parcel): LoginFragment {
            return LoginFragment(parcel)
        }
        override fun newArray(size: Int): Array<LoginFragment?> {
            return arrayOfNulls(size)
        }
    }

}