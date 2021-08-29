package com.demo.doccloud.ui.edit

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.demo.doccloud.R
import com.demo.doccloud.databinding.FragmentEditCropBinding
import com.demo.doccloud.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class EditCropFragment : Fragment() {

    private val viewModel: EditViewModel by navGraphViewModels(R.id.edit_navigation)
    private var _binding: FragmentEditCropBinding? = null
    private val binding get() = _binding!!

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
        loadSelectedPhoto()
    }

    private fun loadSelectedPhoto() {
        Glide.with(binding.root.context).load(viewModel.selectedPhoto?.path)
            .thumbnail(0.01f)
            .fitCenter()
            .into(binding.photo)
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
        viewModel.selectedPhoto?.let {
            val position = it.id
            binding.toolbar.title =
                if (position + 1 < 10) "0${(position + 1)}" else (position + 1).toString()
        }
    }


}