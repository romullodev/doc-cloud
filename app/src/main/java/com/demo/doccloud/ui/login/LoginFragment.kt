package com.demo.doccloud.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult.ACTION_INTENT_SENDER_REQUEST
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult.EXTRA_INTENT_SENDER_REQUEST
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.demo.doccloud.R
import com.demo.doccloud.databinding.FragmentLoginBinding
import com.demo.doccloud.utils.errorDismiss
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private lateinit var loginGoogleContract: ActivityResultContract<IntentSenderRequest, ActivityResult>
    private lateinit var loginGoogleCallback: ActivityResultCallback<ActivityResult>
    private lateinit var loginGoogleLauncher: ActivityResultLauncher<IntentSenderRequest>

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
        //this code below could be changed to loginGoogleContract = StartIntentSenderForResult()
        //this is just for a default implementation
        loginGoogleContract =
            object : ActivityResultContract<IntentSenderRequest, ActivityResult>() {
                override fun createIntent(context: Context, input: IntentSenderRequest): Intent {
                    //Log.d(TAG, “createIntent() called”)
                    return Intent(ACTION_INTENT_SENDER_REQUEST)
                        .putExtra(EXTRA_INTENT_SENDER_REQUEST, input);
                }

                override fun parseResult(resultCode: Int, intent: Intent?): ActivityResult {
                    //Log.d(LOG_TAG, “parseResult() called”)
                    return ActivityResult(resultCode, intent)
                }
            }

        //could get result  on registerForActivityResult directly by lambda function
        loginGoogleCallback = ActivityResultCallback<ActivityResult> { result: ActivityResult? ->
            //Log.d(LOG_TAG, “onActivityResult() called with result: $result”)
            result?.data
        }

        loginGoogleLauncher = registerForActivityResult(loginGoogleContract, loginGoogleCallback)
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

            val request = GetSignInIntentRequest.builder()
                //.setServerClientId(getString(R.string.server_client_id))
                .setServerClientId(getString(R.string.default_web_client_id))
                .build()

            Identity.getSignInClient(requireActivity())
                .getSignInIntent(request)
                .addOnSuccessListener { result ->
                    loginGoogleLauncher.launch(
                        IntentSenderRequest.Builder(result.intentSender)
                            .build()
                    )
                }
                .addOnFailureListener {
                    //e -> Log.e(TAG, "Google Sign-in failed", e)
                }
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
                }
            }
        })
    }

    //helper method to initialize validation fields (for empty fields)
    private fun initValidationFields() = mapOf(
        LoginViewModel.INPUT_INVALID_LOGIN.first to binding.inputLayoutLogin,
        LoginViewModel.INPUT_INVALID_LOGIN_PASSWORD.first to binding.inputLayoutLoginPassword
    )

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}