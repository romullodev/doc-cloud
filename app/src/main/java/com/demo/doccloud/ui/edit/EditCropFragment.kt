package com.demo.doccloud.ui.edit

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.demo.doccloud.R
import com.demo.doccloud.databinding.FragmentEditCropBinding
import com.demo.doccloud.ui.dialogs.doc.CatchDocNameDialog
import com.demo.doccloud.ui.home.HomeViewModel
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import dagger.hilt.android.AndroidEntryPoint
import java.io.File


@AndroidEntryPoint
class EditCropFragment : Fragment() {

    private val viewModel: EditViewModel by navGraphViewModels(R.id.edit_navigation)
    private var _binding: FragmentEditCropBinding? = null
    private val binding get() = _binding!!

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
    }

    private fun setupCropper() {
        cropperLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    CropImage.getActivityResult(result.data)?.let { cropResult ->
                        viewModel.updateDocPhoto(cropResult.uri, requireContext())
                    }
                }
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
        val navController = findNavController()
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


}