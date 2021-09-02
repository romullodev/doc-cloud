package com.demo.doccloud.ui.edit

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.window.WindowManager
import com.demo.doccloud.R
import com.demo.doccloud.adapters.EditAdapter
import com.demo.doccloud.databinding.EditFragmentBinding
import com.demo.doccloud.domain.BackToRoot
import com.demo.doccloud.domain.Photo
import com.demo.doccloud.domain.RootDestination
import com.demo.doccloud.ui.MainActivity
import com.demo.doccloud.ui.dialogs.doc.CatchDocNameDialog
import com.demo.doccloud.utils.Global
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class EditFragment : Fragment() {

    private val viewModel: EditViewModel by navGraphViewModels(R.id.edit_navigation) { defaultViewModelProviderFactory }
    private val args: EditFragmentArgs by navArgs()
    private var _binding: EditFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = EditFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBindingVariables()
        setupObservables()
        setupToolbar()
        setupOnCLickListener()
    }

    override fun onResume() {
        super.onResume()
        //load doc from database
        viewModel.getDocById(args.docLocalId)
    }

    private fun setupOnCLickListener() {
        binding.toolbarTitle.setOnClickListener {
            val materialDialog = CatchDocNameDialog.newInstance(
                object : CatchDocNameDialog.DialogDocNameListener {
                    override fun onSaveClick(docName: String, dialog: DialogFragment) {
                        binding.toolbarTitle.text = docName
                        viewModel.updateNameDoc(
                            localId = args.docLocalId,
                            remoteId = args.docRemoteId,
                            docName
                        )
                        dialog.dismiss()
                    }

                    override fun onCancelClick(dialog: DialogFragment) {
                        dialog.dismiss()
                    }
                },
                getString(R.string.edit_screen_rename_doc_dialog)
            )

            materialDialog.show(
                requireActivity().supportFragmentManager,
                null
            )
        }

        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item?.itemId == R.id.edit_share) {
                viewModel.shareDoc()
                return@setOnMenuItemClickListener true
            }
            return@setOnMenuItemClickListener true
        }

        binding.fab.setOnClickListener {
            viewModel.navigate(
                EditFragmentDirections.actionGlobalCameraFragment(
                    root = BackToRoot(
                        rootDestination = RootDestination.EDIT_DESTINATION,
                        localId = viewModel.doc.value?.localId
                    )
                )
            )
        }
    }

    private fun setupToolbar() {
        //set white color for share icon
        var shareIcon: Drawable = binding.toolbar.menu.findItem(R.id.edit_share).icon
        shareIcon = DrawableCompat.wrap(shareIcon)
        DrawableCompat.setTint(shareIcon, ContextCompat.getColor(requireContext(), R.color.white))
        binding.toolbar.menu.findItem(R.id.edit_share).icon = shareIcon

        //setup our navigation system for this toolbar
        val navController = findNavController()
        binding.toolbar.setupWithNavController(
            navController,
            AppBarConfiguration(navController.graph)
        )
    }

    private fun setupObservables() {
        viewModel.editState.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is EditViewModel.EditState.SharePdf -> {
                        Global.sharedPdfDoc(
                            file = state.data,
                            context = requireContext(),
                            act = requireActivity() as MainActivity
                        )
                    }
                }
            }
        }

        viewModel.doc.observe(viewLifecycleOwner) {
            //set into toolbar doc name
            binding.toolbarTitle.text = it.name
            //viewModel.keepDocPhotos(mutablePhotos)
            (binding.editContent.rvDocPhotos.adapter as EditAdapter).submitList(it.pages.toMutableList())
        }

        viewModel.navigationCommands.observe(viewLifecycleOwner, {
            it.getContentIfNotHandled()
                ?.let { command -> // Only proceed if the event has never been handled
                    when (command) {
                        is EditViewModel.NavigationCommand.To -> {
                            findNavController().navigate(command.directions)
                        }
                    }
                }
        })
    }

    private fun setupBindingVariables() {
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.editContent.rvDocPhotos.apply {
            adapter = EditAdapter(
                widthScreen = WindowManager(binding.root.context).getCurrentWindowMetrics().bounds.width(),
                onClick = object : EditAdapter.OnEditClickListener {
                    override fun onEditClick(photo: Photo, view: View) {
                        viewModel.setSelectedPhoto(photo)
                        viewModel.navigate(EditFragmentDirections.actionEditFragmentToEditCropFragment())
                    }
                }
            )
            layoutManager = GridLayoutManager(
                activity,
                2,
                GridLayoutManager.VERTICAL,
                false
            )
        }
    }

}