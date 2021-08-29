package com.demo.doccloud.ui.edit

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
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
import com.demo.doccloud.domain.Photo
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class EditFragment : Fragment() {

    private val viewModel: EditViewModel by navGraphViewModels(R.id.edit_navigation) { defaultViewModelProviderFactory }
    private val args: EditFragmentArgs by navArgs()
    private var _binding: EditFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //load doc from database
        viewModel.getDocById(args.docId)
    }

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
    }

    private fun setupToolbar() {
        //set white color for share icon
        var shareIcon: Drawable = binding.toolbar.menu.findItem(R.id.edit_share).icon
        shareIcon = DrawableCompat.wrap(shareIcon)
        DrawableCompat.setTint(shareIcon, ContextCompat.getColor(requireContext(), R.color.white))
        binding.toolbar.menu.findItem(R.id.edit_share).icon = shareIcon
        //set white color for edit icon
        var editIcon: Drawable = binding.toolbar.menu.findItem(R.id.edit_name).icon
        editIcon = DrawableCompat.wrap(editIcon)
        DrawableCompat.setTint(editIcon, ContextCompat.getColor(requireContext(), R.color.white))
        binding.toolbar.menu.findItem(R.id.edit_name).icon = editIcon

        //setup our navigation system for this toolbar
        val navController = findNavController()
        binding.toolbar.setupWithNavController(
            navController,
            AppBarConfiguration(navController.graph)
        )

    }

    private fun setupObservables() {
        viewModel.doc.observe(viewLifecycleOwner) {
            //set into toolbar doc name
            binding.toolbar.title = it.name
            val photos: List<Photo> = it.pages.mapIndexed { index, data ->
                Photo(
                    id = index.toLong(),
                    path = data
                )
            }
            val mutablePhotos = photos.toMutableList()

            viewModel.setNewPhotos(mutablePhotos)
            (binding.editContent.rvDocPhotos.adapter as EditAdapter).submitList(mutablePhotos)
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
                        viewModel.selectedPhoto = photo
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