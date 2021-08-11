package com.demo.doccloud.ui.home

//import androidx.appcompat.widget.PopupMenu
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.demo.doccloud.R
import com.demo.doccloud.adapters.DocAdapter
import com.demo.doccloud.databinding.HomeDialogNewDocBinding
import com.demo.doccloud.databinding.HomeFragmentBinding
import com.demo.doccloud.domain.Doc
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.lang.reflect.Method


@AndroidEntryPoint
class HomeFragment : Fragment(), PopupMenu.OnMenuItemClickListener {
    private var _binding: HomeFragmentBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by viewModels()

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
        setupObservers()
        setupBindingVariable()
        setupListeners()
        setupToolbar()
    }

    private fun setupToolbar() {
        binding.toolbar.overflowIcon?.setTint(Color.WHITE)
        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item?.itemId == R.id.logout) {
                homeViewModel.doLogout()
                return@setOnMenuItemClickListener true
            }
            return@setOnMenuItemClickListener true
        }

        val item = binding.toolbar.menu.findItem(R.id.search)
        val searchView = item.actionView as SearchView

        val editText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        editText.setTextColor(ContextCompat.getColor(binding.root.context, R.color.white))
        editText.setHintTextColor(ContextCompat.getColor(binding.root.context, R.color.a_60_white))
        editText.hint = "Pesquisar"
        searchView.imeOptions = EditorInfo.IME_ACTION_DONE
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isEmpty()) {
                    adapter.filter.filter("")
                } else {
                    adapter.filter.filter(newText)
                }
                return true
            }
        })

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
//        binding.logoutButton.setOnClickListener {
//            homeViewModel.doLogout()
//        }
        binding.addButton.setOnClickListener {
            val popup = PopupMenu(requireContext(), it)
            val inflater: MenuInflater = popup.menuInflater
            inflater.inflate(R.menu.home_new_doc_menu, popup.menu)
            popup.setOnMenuItemClickListener(this)
            forceIconOnMenu(popup)
            popup.show()
        }


    }

    private fun setupBindingVariable() {
        binding.homeViewModel = homeViewModel
        //require to kill binding objects when this fragment is destroyed (lifecycle aware)
        binding.lifecycleOwner = viewLifecycleOwner
        adapter = DocAdapter(
            arrayListOf(
                Doc(1, "nome 1", "10/10/2021", "enviado"),
                Doc(2, "nome 1", "10/10/2021", "enviado"),
                Doc(3, "nome 1", "10/10/2021", "enviado"),
                Doc(4, "nome 1", "10/10/2021", "enviado"),
                Doc(5, "nome 1", "10/10/2021", "enviado"),
                Doc(6, "nome 1", "10/10/2021", "enviado"),
                Doc(7, "nome 1", "10/10/2021", "enviado"),
                Doc(8, "nome 1", "10/10/2021", "enviado"),
                Doc(9, "nome 1", "10/10/2021", "enviado"),
                Doc(10, "nome 1", "10/10/2021", "enviado"),
                Doc(11, "nome 1", "10/10/2021", "enviado"),
                Doc(12, "nome 1", "10/10/2021", "enviado"),
                Doc(13, "nome 1", "10/10/2021", "enviado"),
                Doc(14, "nome 1", "10/10/2021", "enviado"),
                Doc(15, "nome 1", "10/10/2021", "enviado"),
                Doc(16, "nome 1", "10/10/2021", "enviado")
            ),
            object : DocAdapter.OnDocClickListener {
                override fun onDocClick(doc: Doc, view: View) {
                    val popup = PopupMenu(requireContext(), view)
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
                Toast.makeText(requireContext(), "Compartilhar", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.edit -> {
                Toast.makeText(requireContext(), "Editar", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.delete -> {
                Toast.makeText(requireContext(), "Excluir", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.newDoc -> {
                val dialog = MaterialAlertDialogBuilder(
                    requireContext(),
                    R.style.ThemeOverlay_App_MaterialAlertDialog
                ).create()//AlertDialog.Builder(requireContext()).create()
                val layoutInflater = LayoutInflater.from(requireContext())
                val view = HomeDialogNewDocBinding.inflate(layoutInflater, null, false)
                view.btnClose.setOnClickListener {
                    dialog.dismiss()
                }
                dialog.setView(view.root)
                dialog.show()
                true
            }
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

}