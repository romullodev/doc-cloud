package com.demo.doccloud.ui.edit

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.demo.doccloud.R
import com.demo.doccloud.databinding.FragmentEditCropBinding
import com.demo.doccloud.ui.dialogs.alert.AppAlertDialog
import com.demo.doccloud.utils.AppConstants
import com.demo.doccloud.utils.DialogsHelper
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import dagger.hilt.android.AndroidEntryPoint
import java.io.File


@AndroidEntryPoint
class EditCropFragment() : Fragment(), AppAlertDialog.DialogMaterialListener {

    private val viewModel: EditViewModel by navGraphViewModels(R.id.edit_navigation)
    private var _binding: FragmentEditCropBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController

    //to launch cropper activity
    private lateinit var cropperLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditCropBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupListeners()
        setupCropper()
        setupObservables()
    }

    private fun setupObservables() {
        viewModel.selectedPhoto.observe(viewLifecycleOwner){
            Glide.with(binding.root.context).load(it.path)
                .thumbnail(0.01f)
                .fitCenter()
                .into(binding.photo)
        }

        viewModel.cropState.observe(viewLifecycleOwner){
            it.getContentIfNotHandled()?.let { state->
                when(state){
                    is EditViewModel.CropState.CropAlertDialog -> {
                        DialogsHelper.showAlertDialog(
                            DialogsHelper.getInfoAlertParams(
                                msg = getString(state.msg)
                            ),
                            this,
                            requireActivity(),
                            tag = AppConstants.INFO_DIALOG_TAG
                        )
                    }
                }
            }
        }
    }

    private fun setupCropper() {
        cropperLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                val uri: Uri? = CropImage.getActivityResult(result.data)?.uri
                viewModel.updateDocPhoto(uri)
            }
    }

    private fun setupListeners() {
        binding.toolbar.setOnMenuItemClickListener { item->
            if (item?.itemId == R.id.edit_crop) {
                val file = File(viewModel.getSelectedPhoto()?.path ?: return@setOnMenuItemClickListener true)
                val uri = Uri.fromFile(file)
                val intent = CropImage
                    .activity(uri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setCropShape(CropImageView.CropShape.RECTANGLE)
                    .getIntent(requireContext())

                cropperLauncher.launch(intent)
                return@setOnMenuItemClickListener true
            }
            if (item?.itemId == R.id.edit_delete) {
                DialogsHelper.showAlertDialog(
                    DialogsHelper.getQuestionDeleteAlertParams(
                        msg = "Deseja realmente excluir?"
                    ),
                    object : AppAlertDialog.DialogMaterialListener{
                        override fun onDialogPositiveClick(dialog: DialogFragment) {
                            viewModel.deleteSelectedDocPhoto()
                            activity?.runOnUiThread{
                                navController.popBackStack()
                            }
                            dialog.dismiss()
                        }

                        override fun onDialogNegativeClick(dialog: DialogFragment) {
                            dialog.dismiss()
                        }
                    },
                    requireActivity(),
                    tag = AppConstants.QUESTION_DIALOG_TAG
                )
                return@setOnMenuItemClickListener true
            }

            return@setOnMenuItemClickListener true
        }
    }

    private fun setupToolbar() {
        //set white color for crop icon
        var cropIcon: Drawable = binding.toolbar.menu.findItem(R.id.edit_crop).icon
        cropIcon = DrawableCompat.wrap(cropIcon)
        DrawableCompat.setTint(cropIcon, ContextCompat.getColor(requireContext(), R.color.white))
        binding.toolbar.menu.findItem(R.id.edit_crop).icon = cropIcon

        //setup our navigation system for this toolbar
        navController = findNavController()
        binding.toolbar.setupWithNavController(
            navController,
            AppBarConfiguration(navController.graph)
        )
        //setup title
        viewModel.getSelectedPhoto()?.let {
            val position = it.id
            binding.toolbar.title =
                if (position + 1 < 10) "0${(position + 1)}" else (position + 1).toString()
        }
    }

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        dialog.dismiss()
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        dialog.dismiss()
    }

    constructor(parcel: Parcel) : this()

    override fun describeContents(): Int {
        return 0
    }
    companion object CREATOR : Parcelable.Creator<EditCropFragment> {
        override fun createFromParcel(parcel: Parcel): EditCropFragment {
            return EditCropFragment(parcel)
        }

        override fun newArray(size: Int): Array<EditCropFragment?> {
            return arrayOfNulls(size)
        }
    }

}