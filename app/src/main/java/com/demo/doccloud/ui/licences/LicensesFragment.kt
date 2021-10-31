package com.demo.doccloud.ui.licences

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.demo.doccloud.R
import com.demo.doccloud.databinding.LicensesFragmentBinding
import com.demo.doccloud.ui.dialogs.alert.AppAlertDialog
import com.demo.doccloud.ui.licences.adapters.AppLicenseAdapter
import com.demo.doccloud.utils.DialogsHelper
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp

@AndroidEntryPoint
class LicensesFragment : Fragment() {

    private var _binding: LicensesFragmentBinding? = null
    val binding get() = _binding!!

    private val licensesViewModel: LicensesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        licensesViewModel.syncLicenses()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LicensesFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupBindings()
        setupAdapter()
        setupToolbar()
    }

    private fun setupToolbar() {
        val navController = findNavController()
        binding.toolbar.setupWithNavController(
            navController,
            AppBarConfiguration(navController.graph)
        )
    }

    private fun setupAdapter() {
        binding.recyclerView.adapter = AppLicenseAdapter()
    }

    private fun setupObservers() {
        licensesViewModel.licensesState.observe(viewLifecycleOwner){
            it.getContentIfNotHandled()?.let { state ->
                when(state){
                    is LicensesViewModel.LicensesState.LicensesAlertDialog -> {
                        DialogsHelper.showAlertDialog(
                            DialogsHelper.getInfoAlertParams(
                                msg = getString(state.msg)
                            ),
                            object: AppAlertDialog.DialogMaterialListener {
                                override fun onDialogPositiveClick(dialog: DialogFragment) {
                                    findNavController().popBackStack()
                                    dialog.dismiss()
                                }

                                override fun onDialogNegativeClick(dialog: DialogFragment) {
                                    dialog.dismiss()
                                }
                            },
                            requireActivity(),
                        )
                    }
                }
            }
        }
    }

    private fun setupBindings() {
        binding.licensesViewModel = licensesViewModel
        binding.lifecycleOwner = this
    }

}