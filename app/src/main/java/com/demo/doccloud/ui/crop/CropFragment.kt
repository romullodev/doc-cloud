package com.demo.doccloud.ui.crop

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.demo.doccloud.R
import com.demo.doccloud.adapters.CropAdapter
import com.demo.doccloud.databinding.CropFragmentBinding
import com.demo.doccloud.domain.Photo
import com.demo.doccloud.domain.RootDestination
import com.demo.doccloud.ui.dialogs.alert.AppAlertDialog
import com.demo.doccloud.ui.dialogs.doc.CatchDocNameDialog
import com.demo.doccloud.utils.DialogsHelper
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class CropFragment() : Fragment() {

    private val args: CropFragmentArgs by navArgs()
    private var _binding: CropFragmentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CropViewModel by viewModels()
    private lateinit var cropAdapter: CropAdapter
    private lateinit var navController: NavController

    //to launch cropper activity
    private lateinit var cropperLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navController = findNavController()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CropFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCropper()
        setupObservers()
        setupBindingVariables()
        setupListeners()
        setupToolbar()
    }

    private fun setupToolbar() {
        binding.toolbar.setupWithNavController(
            navController,
            AppBarConfiguration(navController.graph)
        )
        //delete all photos in case of pick images from gallery
        binding.toolbar.setNavigationOnClickListener {
            if(navController.previousBackStackEntry?.destination?.id == R.id.homeFragment){
             viewModel.deleteAllPhotos()
            }
            navController.popBackStack()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if(navController.previousBackStackEntry?.destination?.id == R.id.homeFragment){
                viewModel.deleteAllPhotos()
            }
            navController.popBackStack()
        }
    }

    private fun setupListeners() {
        binding.continueBtn.setOnClickListener {
            when(args.root.rootDestination){
                RootDestination.HOME_DESTINATION -> {
                    val materialDialog = CatchDocNameDialog.newInstance(
                        object : CatchDocNameDialog.DialogDocNameListener {
                            override fun onSaveClick(docName: String, dialog: DialogFragment) {
                                viewModel.saveDocs(docName)
                                dialog.dismiss()

                            }

                            override fun onCancelClick(dialog: DialogFragment) {
                                dialog.dismiss()
                            }
                        },
                        getString(R.string.crop_screen_dialog_title_label)
                    )

                    materialDialog.show(
                        requireActivity().supportFragmentManager,
                        null
                    )
                }
                RootDestination.EDIT_DESTINATION -> {
                    viewModel.addPhotos(args.root.localId)
                }
            }
        }
    }

    private fun setupCropper() {
        cropperLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    CropImage.getActivityResult(result.data)?.let { cropResult ->
                        viewModel.saveCropPhoto(cropResult.uri, requireContext())
                    }
                }
            }
    }

    private fun setupObservers() {
        viewModel.listPhoto.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                cropAdapter.submitList(it.toMutableList())
            } else {
                navController.popBackStack()
            }
        }
        viewModel.cropState.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is CropViewModel.CropState.CropAlertDialog -> {
                        DialogsHelper.showAlertDialog(
                            DialogsHelper.getInfoAlertParams(msg = state.msg),
                            object : AppAlertDialog.DialogMaterialListener {
                                override fun onDialogPositiveClick(dialog: DialogFragment) {
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

        viewModel.navigationCommands.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is CropViewModel.NavigationCommand.ToRoot -> {
                        when(args.root.rootDestination){
                            RootDestination.HOME_DESTINATION -> {
                                navController.popBackStack(R.id.homeFragment, false)
                            }
                            RootDestination.EDIT_DESTINATION -> {
                                navController.popBackStack(R.id.editFragment, false)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupBindingVariables() {
        viewModel.setListPhoto(args.photos.list)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        when(args.root.rootDestination){
            RootDestination.HOME_DESTINATION -> {
                binding.textButton = getString(R.string.crop_screen_continue_label)
            }
            RootDestination.EDIT_DESTINATION -> {
                binding.textButton = getString(R.string.crop_screen_add_label)
            }
        }


        cropAdapter = CropAdapter(object : CropAdapter.OnCropClickListener {
            override fun onCropClick(photo: Photo, position: Int) {
                //this position will be used for track this photo on viewModel
                viewModel.setCurrCroppedPosition(position)
                val file = File(photo.path)
                val uri = Uri.fromFile(file)
                val intent = CropImage
                    .activity(uri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setCropShape(CropImageView.CropShape.RECTANGLE)
                    .getIntent(requireContext())

                cropperLauncher.launch(intent)
            }

            override fun onDeleteClick(photo: Photo) {
                viewModel.removePhoto(photo)
            }
        })
        binding.rvCrop.adapter = cropAdapter
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    companion object CREATOR : Parcelable.Creator<CropFragment> {
        override fun createFromParcel(parcel: Parcel): CropFragment {
            return CropFragment(parcel)
        }

        override fun newArray(size: Int): Array<CropFragment?> {
            return arrayOfNulls(size)
        }
    }

    constructor(parcel: Parcel) : this() {
    }
}