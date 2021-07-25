package com.demo.doccloud.ui

import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment.findNavController
import com.demo.doccloud.R
import com.demo.doccloud.databinding.ActivityMainBinding
import com.demo.doccloud.utils.setupToolbar
import com.google.android.material.appbar.MaterialToolbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val toolbar: MaterialToolbar = binding.contentMain.toolbar
        setSupportActionBar(toolbar)
        val navController = findNavController(R.id.nav_host_fragment)
        toolbar.setupToolbar(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.loginFragment) {
                binding.contentMain.appBar.visibility = View.GONE
            } else {
                binding.contentMain.appBar.visibility = View.VISIBLE

            }
        }
    }
}
