package com.demo.doccloud.ui.home

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.demo.doccloud.R
import com.demo.doccloud.ui.home.adapters.DocAdapter
import com.demo.doccloud.databinding.HomeDialogNewDocBinding
import com.demo.doccloud.databinding.HomeFragmentBinding
import com.demo.doccloud.domain.*
import com.demo.doccloud.utils.BackToRoot
import com.demo.doccloud.domain.entities.Doc
import com.demo.doccloud.utils.RootDestination
import com.demo.doccloud.ui.MainActivity
import com.demo.doccloud.ui.dialogs.alert.AppAlertDialog
import com.demo.doccloud.utils.AppConstants
import com.demo.doccloud.utils.DialogsHelper
import com.demo.doccloud.utils.Global
import com.demo.doccloud.workers.SyncDataWorker
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.lang.reflect.Method
import java.util.*
import kotlin.collections.ArrayList


@AndroidEntryPoint
class HomeFragment() :
    Fragment(),
    PopupMenu.OnMenuItemClickListener,
    AppAlertDialog.DialogMaterialListener {
    private var _binding: HomeFragmentBinding? = null
    private val binding get() = _binding!!

    //workaround on searchView for configuration changes
    //problem:  when configuration changes, setOnQueryTextListener triggers making data just go away
    private var searchViewHasTrigger = false

    private val homeViewModel: HomeViewModel by activityViewModels()

    //to launch gallery
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>

    private lateinit var adapter: DocAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = HomeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBindingVariable()
        setupListeners()
        setupObservers()
        setupToolbar()
        setupGalleryLauncher()
    }

    private fun getFormattedSubtitle(): String{
        val now = Calendar.getInstance()
        val dayOfWeek = getDayOfWeek(now[Calendar.DAY_OF_WEEK])
        val month = getMonth(now[Calendar.MONTH])
        //val subtitle = resources.getString(R.string.home_welcome_subtitle)
        return getString(R.string.home_welcome_subtitle, dayOfWeek, now[Calendar.DAY_OF_MONTH].toString(), month)//String.format(subtitle, dayOfWeek, now[Calendar.DAY_OF_MONTH], month)
    }

    private fun getMonth(month: Int): String{
        return when(month){
            0 -> getString(R.string.home_month_january)
            1 -> getString(R.string.home_month_february)
            2 -> getString(R.string.home_month_march)
            3 -> getString(R.string.home_month_april)
            4 -> getString(R.string.home_month_may)
            5 -> getString(R.string.home_month_june)
            6 -> getString(R.string.home_month_july)
            7 -> getString(R.string.home_month_august)
            8 -> getString(R.string.home_month_september)
            9 -> getString(R.string.home_month_october)
            10 -> getString(R.string.home_month_november)
            else -> getString(R.string.home_month_december)
        }
    }

    private fun getDayOfWeek(week: Int): String{
        return when(week){
            1 -> getString(R.string.home_week_sunday)
            2 -> getString(R.string.home_week_monday)
            3 -> getString(R.string.home_week_tuesday)
            4 -> getString(R.string.home_week_wednesday)
            5 -> getString(R.string.home_week_thursday)
            6 -> getString(R.string.home_week_friday)
            else -> getString(R.string.home_week_saturday)
        }
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
                        homeViewModel.copyAndNavigateToCrop(uris)
                    }
                    intent.data?.let { uri ->
                        homeViewModel.copyAndNavigateToCrop(listOf(uri))
                    }
                }
            }
    }

    private fun setupToolbar() {
        //set 3 dots color to white
        //binding.toolbar.overflowIcon?.setTint(Color.WHITE)
        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item?.itemId == R.id.logout) {
                homeViewModel.doLogout()
                return@setOnMenuItemClickListener true
            }
            return@setOnMenuItemClickListener true
        }

        val item = binding.toolbar.menu.findItem(R.id.search)
        val searchView = item.actionView as SearchView

        val editText =
            searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        editText.setTextColor(ContextCompat.getColor(binding.root.context, R.color.white))
        editText.setHintTextColor(
            ContextCompat.getColor(
                binding.root.context,
                R.color.a_60_white
            )
        )
        editText.hint = "Pesquisar"
        searchView.apply {
            setOnQueryTextFocusChangeListener { _, hasFocus ->
                searchViewHasTrigger = hasFocus
            }
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    if (searchViewHasTrigger) {
                        if (newText.isEmpty()) {
                            adapter.filter.filter("")
                        } else {
                            adapter.filter.filter(newText)
                        }
                    }
                    return true
                }
            })
            imeOptions = EditorInfo.IME_ACTION_DONE
        }

        //reference: https://stackoverflow.com/questions/31662416/show-collapsingtoolbarlayout-title-only-when-collapsed
        binding.appBar.addOnOffsetChangedListener(object : OnOffsetChangedListener {
            var isShow = true
            var scrollRange = -1
            override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.totalScrollRange
                }
                if (scrollRange + verticalOffset == 0) {
                    binding.toolbar.title = "Início"
                    isShow = true
                } else if (isShow) {
                    binding.toolbar.title =
                        " " //careful there should a space between double quote otherwise it wont work
                    isShow = false
                }
            }
        })
    }

    private fun setupListeners() {
        binding.addButton.setOnClickListener {
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
                homeViewModel.navigate(
                    HomeFragmentDirections.actionHomeFragmentToCameraFragment(
                        root = BackToRoot(rootDestination = RootDestination.HOME_DESTINATION)
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

            /*
                val popup = PopupMenu(requireContext(), it)
                val inflater: MenuInflater = popup.menuInflater
                inflater.inflate(R.menu.home_new_doc_menu, popup.menu)
                popup.setOnMenuItemClickListener(this)
                forceIconOnMenu(popup)
                popup.show()
                 */
        }
    }

    private fun setupBindingVariable() {
        binding.homeViewModel = homeViewModel
        //require to kill binding objects when this fragment is destroyed (lifecycle aware)
        binding.lifecycleOwner = viewLifecycleOwner
        adapter = DocAdapter(
            object : DocAdapter.OnDocClickListener {
//                override fun onMoreOptionsClick(doc: Doc, view: View) {
//                    //this doc could be used to share/delete/edit on onMenuItemClick() method
//                    homeViewModel.currDoc = doc
//                    val popup = PopupMenu(requireContext(), view)
//                    val inflater: MenuInflater = popup.menuInflater
//                    inflater.inflate(R.menu.home_doc_item, popup.menu)
//                    popup.setOnMenuItemClickListener(this@HomeFragment)
//                    forceIconOnMenu(popup)
//                    popup.show()
//                }

                override fun onDocClick(doc: Doc) {
                    homeViewModel.navigate(
                        HomeFragmentDirections.actionHomeFragmentToEditFragment(
                            docLocalId = doc.localId,
                            docRemoteId = doc.remoteId
                        )
                    )
                }

                override fun onLongDocClick(doc: Doc, view: View) {
                    //this doc could be used to share/delete/edit on onMenuItemClick() method
                    homeViewModel.currDoc = doc
                    val popup = PopupMenu(requireContext(), view, GravityCompat.END)
                    val inflater: MenuInflater = popup.menuInflater
                    inflater.inflate(R.menu.home_doc_item, popup.menu)
                    popup.setOnMenuItemClickListener(this@HomeFragment)
                    forceIconOnMenu(popup)
                    popup.show()
                }
            }
        )
        binding.rvHome.adapter = adapter
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.share -> {
                homeViewModel.shareDoc()
                true
            }
//            R.id.edit -> {
//                homeViewModel.navigate(
//                    HomeFragmentDirections.actionHomeFragmentToEditFragment(
//                        docLocalId = homeViewModel.currDoc?.localId!!,
//                        docRemoteId = homeViewModel.currDoc?.remoteId!!
//                    )
//                )
//                true
//            }
            R.id.delete -> {
                DialogsHelper.showAlertDialog(
                    DialogsHelper.getQuestionDeleteAlertParams(
                        msg = "Deseja realmente excluir?"
                    ),
                    this,
                    requireActivity(),
                    tag = AppConstants.QUESTION_DIALOG_TAG
                )
                true
            }
//            R.id.newDoc -> {
//                val dialog = MaterialAlertDialogBuilder(
//                    requireContext(),
//                    R.style.ThemeOverlay_App_MaterialAlertDialog
//                ).create()//AlertDialog.Builder(requireContext()).create()
//                val layoutInflater = LayoutInflater.from(requireContext())
//                val view = HomeDialogNewDocBinding.inflate(layoutInflater, null, false)
//                view.btnClose.setOnClickListener {
//                    dialog.dismiss()
//                }
//                view.cameraTv.setOnClickListener {
//                    homeViewModel.navigate(HomeFragmentDirections.actionHomeFragmentToCameraFragment())
//                    dialog.dismiss()
//                }
//                dialog.setView(view.root)
//                dialog.show()
//                true
//            }
            else -> false
        }
    }

    private fun setupObservers() {
        homeViewModel.navigationCommands.observe(viewLifecycleOwner, {
            it.getContentIfNotHandled()
                ?.let { command -> // Only proceed if the event has never been handled
                    when (command) {
                        is HomeViewModel.NavigationCommand.To -> {
                            findNavController().navigate(command.directions)
                        }
                    }
                }
        })

        homeViewModel.homeState.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is HomeViewModel.HomeState.HomeAlertDialog -> {
                        DialogsHelper.showAlertDialog(
                            DialogsHelper.getInfoAlertParams(
                                msg = getString(state.msg)
                            ),
                            this,
                            requireActivity(),
                            tag = AppConstants.INFO_DIALOG_TAG
                        )
                    }
                    is HomeViewModel.HomeState.HomeToastMessage -> {
                        Toast.makeText(context, state.msg, Toast.LENGTH_SHORT).show()
                    }
                    is HomeViewModel.HomeState.SharePdf -> {
                        Global.sharedPdfDoc(
                            file = state.data,
                            context = requireContext(),
                            act = requireActivity() as MainActivity
                        )
                    }
                }
            }
        }

        Global.user.observe(viewLifecycleOwner){
            it.getContentIfNotHandled()?.let { user->
                binding.title.text = context?.getString(R.string.home_welcome_title, user.displayName)
                binding.subtitle.text = getFormattedSubtitle()
            }
        }
        //observe from SyncDataWorker to update view when sync data
        SyncDataWorker.syncDataProgress.observe(viewLifecycleOwner) {
            //this will be improved soon
            if (it != -1L) {
                binding.syncDataProgress.visibility = View.VISIBLE
            } else {
                binding.syncDataProgress.visibility = View.GONE
            }
        }
    }

    private fun forceIconOnMenu(popup: PopupMenu) {
        //reference: https://android--code.blogspot.com/2020/06/android-kotlin-popup-menu-with-icons.html
        // show icons on popup menu
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popup.setForceShowIcon(true)
        } else {
            try {
                val fields = popup.javaClass.declaredFields
                for (field in fields) {
                    if ("mPopup" == field.name) {
                        field.isAccessible = true
                        val menuPopupHelper = field[popup]
                        val classPopupHelper =
                            Class.forName(menuPopupHelper.javaClass.name)
                        val setForceIcons: Method = classPopupHelper.getMethod(
                            "setForceShowIcon",
                            Boolean::class.javaPrimitiveType
                        )
                        setForceIcons.invoke(menuPopupHelper, true)
                        break
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        when (dialog.tag) {
            AppConstants.QUESTION_DIALOG_TAG -> homeViewModel.deleteDoc()
        }
        dialog.dismiss()
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        dialog.dismiss()
    }

    constructor(parcel: Parcel) : this() {
        searchViewHasTrigger = parcel.readByte() != 0.toByte()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeByte(if (searchViewHasTrigger) 1 else 0)
    }

    companion object CREATOR : Parcelable.Creator<HomeFragment> {
        override fun createFromParcel(parcel: Parcel): HomeFragment {
            return HomeFragment(parcel)
        }

        override fun newArray(size: Int): Array<HomeFragment?> {
            return arrayOfNulls(size)
        }
    }

}