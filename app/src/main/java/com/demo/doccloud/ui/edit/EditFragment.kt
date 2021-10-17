package com.demo.doccloud.ui.edit

import android.app.Activity
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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.window.WindowManager
import com.demo.doccloud.R
import com.demo.doccloud.ui.edit.adapters.EditAdapter
import com.demo.doccloud.databinding.EditFragmentBinding
import com.demo.doccloud.databinding.HomeDialogNewDocBinding
import com.demo.doccloud.domain.entities.Photo
import com.demo.doccloud.ui.MainActivity
import com.demo.doccloud.ui.dialogs.alert.AppAlertDialog
import com.demo.doccloud.ui.dialogs.doc.CatchDocNameDialog
import com.demo.doccloud.utils.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.*

@AndroidEntryPoint
class EditFragment() : Fragment(), AppAlertDialog.DialogMaterialListener {

    private val viewModel: EditViewModel by navGraphViewModels(R.id.edit_navigation) { defaultViewModelProviderFactory }
    private val args: EditFragmentArgs by navArgs()
    private var _binding: EditFragmentBinding? = null
    private val binding get() = _binding!!

    //to launch gallery
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>

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
        setupGalleryLauncher()
    }

    private fun setupGalleryLauncher() {
        galleryLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val intent = result.data ?: return@registerForActivityResult
                    intent.clipData?.let { clipData ->
                        val uris = ArrayList<Uri?>()
                        for (i in 0 until clipData.itemCount) {
                            uris.add(clipData.getItemAt(i).uri)
                        }
                        viewModel.copyAndNavigateToCrop(uris)
                        return@registerForActivityResult
                    }
                    intent.data?.let { uri ->
                        viewModel.copyAndNavigateToCrop(listOf(uri))
                        return@registerForActivityResult
                    }
                    Timber.d("error on get photo from gallery")
                }
            }
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
            val dialog = MaterialAlertDialogBuilder(
                requireContext(),
                R.style.ThemeOverlay_App_MaterialAlertDialog
            ).create()//AlertDialog.Builder(requireContext()).create()
            val layoutInflater = LayoutInflater.from(requireContext())
            val view = HomeDialogNewDocBinding.inflate(layoutInflater, null, false)
            view.btnClose.setOnClickListener {
                dialog.dismiss()
            }
            view.cameraTv.setOnClickListener {
                viewModel.navigate(
                    EditFragmentDirections.actionGlobalCameraFragment(
                        root = BackToRoot(
                            rootDestination = RootDestination.EDIT_DESTINATION,
                            localId = viewModel.doc.value?.localId
                        )
                    )
                )
                dialog.dismiss()
            }
            view.galleryTv.setOnClickListener {
                // For latest versions API LEVEL 19+
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "image/*"
                galleryLauncher.launch(intent)
                dialog.dismiss()
            }
            dialog.setView(view.root)
            dialog.show()
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
                    is EditViewModel.EditState.EditAlertDialog -> {
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

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        dialog.dismiss()
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        dialog.dismiss()
    }

    override fun describeContents(): Int {
        return 0
    }

    constructor(parcel: Parcel) : this()

    companion object CREATOR : Parcelable.Creator<EditFragment> {
        override fun createFromParcel(parcel: Parcel): EditFragment {
            return EditFragment(parcel)
        }

        override fun newArray(size: Int): Array<EditFragment?> {
            return arrayOfNulls(size)
        }
    }
}