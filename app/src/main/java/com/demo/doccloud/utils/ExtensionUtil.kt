package com.demo.doccloud.utils

import androidx.core.view.ViewCompat
import androidx.navigation.NavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.demo.doccloud.R
import com.demo.doccloud.domain.User
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseUser

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

fun FirebaseUser.asDomain() : User {
    return User(
        displayName =  this.displayName ?: "SEM NOME",
        userId = this.uid
    )
}