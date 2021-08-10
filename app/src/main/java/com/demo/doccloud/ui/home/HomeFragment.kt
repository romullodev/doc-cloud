package com.demo.doccloud.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.demo.doccloud.R
import com.demo.doccloud.adapters.DocAdapter
import com.demo.doccloud.databinding.HomeFragmentBinding
import com.demo.doccloud.domain.Doc
import com.demo.doccloud.ui.login.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: HomeFragmentBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by viewModels()

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
    }

    private fun setupListeners() {
        binding.logoutButton.setOnClickListener {
            homeViewModel.doLogout()
        }
    }

//    private fun setupToolbar() {
//        binding.toolbar.overflowIcon?.setTint(Color.WHITE)
//        binding.toolbar.setOnMenuItemClickListener { item ->
//            if (item?.itemId == R.id.logout) {
//                homeViewModel.doLogout()
//                return@setOnMenuItemClickListener true
//            }
//            return@setOnMenuItemClickListener true
//        }
//    }

    private fun setupBindingVariable() {
        binding.homeViewModel = homeViewModel
        //require to kill binding objects when this fragment is destroyed (lifecycle aware)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.rvHome.adapter = DocAdapter(
            arrayListOf(
                Doc(1, "nome 1", "10/10/2021", "enviado"),
                Doc(2, "nome 1", "10/10/2021", "enviado"),
                Doc(3, "nome 1", "10/10/2021", "enviado"),
                Doc(4, "nome 1", "10/10/2021", "enviado")
            ),
            object : DocAdapter.OnDocClickListener {
                override fun onDocClick(doc: Doc, position: Int) {
                    //TODO("Not yet implemented")
                }
            }
        )
    }

//
//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        inflater.inflate(R.menu.home_menu, menu)
//        super.onCreateOptionsMenu(menu, inflater)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if (item.itemId == R.id.logout) {
//            homeViewModel.doLogout()
//            return true
//        }
//        return super.onOptionsItemSelected(item)
//    }

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

}