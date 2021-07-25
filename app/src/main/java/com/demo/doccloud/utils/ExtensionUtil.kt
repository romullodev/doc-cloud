package com.demo.doccloud.utils

import androidx.core.view.ViewCompat
import androidx.navigation.NavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.demo.doccloud.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.textfield.TextInputLayout

// help to setup appbar on every fragment

fun MaterialToolbar.setupToolbar(navController: NavController){
    val appBarConfiguration = AppBarConfiguration(navController.graph)
    this.setupWithNavController(navController, appBarConfiguration)
}

//Widget's extensions
fun TextInputLayout.errorDismiss() {
    this.error = null
    this.isErrorEnabled = false
}