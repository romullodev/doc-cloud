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
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.demo.doccloud.R
import com.demo.doccloud.adapters.CropAdapter
import com.demo.doccloud.databinding.CropFragmentBinding
import com.demo.doccloud.domain.Photo
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
                    CropViewModel.CropState.SaveDocNameDialog -> {
                        val materialDialog = SaveDocNameDialog.newInstance(
                            object : SaveDocNameDialog.DialogDocNameListener {
                                override fun onSaveClick(docName: String, dialog: DialogFragment) {
                                    Toast.makeText(context, "Documento Salvo", Toast.LENGTH_SHORT)
                                        .show()
                                    viewModel.navigate(CropFragmentDirections.actionCropFragmentToHomeFragment())
                                    dialog.dismiss()

                                }

                                override fun onCancelClick(dialog: DialogFragment) {
                                    Toast.makeText(context, "Cancelado", Toast.LENGTH_SHORT).show()
                                    dialog.dismiss()
                                }
                            }
                        )

                        materialDialog.show(
                            requireActivity().supportFragmentManager,
                            null
                        )
                    }
                }
            }
        }

        viewModel.navigationCommands.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is CropViewModel.NavigationCommand.To -> {
                        navController.navigate(state.directions)
                    }
                }
            }
        }
    }

    private fun setupBindingVariables() {
        viewModel.setListPhoto(args.photos.list)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
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