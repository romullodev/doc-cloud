package com.demo.doccloud.utils

import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.demo.doccloud.R
import com.demo.doccloud.domain.Photo
import com.demo.doccloud.domain.User
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseUser
import android.provider.MediaStore




// help to setup appbar on every fragment

fun MaterialToolbar.setupToolbar(navController: NavController) {
    val appBarConfiguration = AppBarConfiguration(navController.graph)
    this.setupWithNavController(navController, appBarConfiguration)
}

//Widget's extensions
fun TextInputLayout.errorDismiss() {
    this.error = null
    this.isErrorEnabled = false
}

fun FirebaseUser.asDomain(): User {
    return User(
        displayName = this.displayName ?: "SEM NOME",
        userId = this.uid
    )
}

fun AppCompatActivity.checkAllSelfPermissionsCompat(permissions: Array<String>) =
    permissions.all {
        ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

fun AppCompatActivity.shouldShowAllRequestPermissionsRationaleCompat(permissions: Array<String>) =
    permissions.all{
        ActivityCompat.shouldShowRequestPermissionRationale(this, it)
    }

fun AppCompatActivity.requestPermissionsCompat(
    permissionsArray: Array<String>,
    requestCode: Int
) {
    ActivityCompat.requestPermissions(this, permissionsArray, requestCode)
}

//liveData's extensions (MutableList version)
fun <T> MutableLiveData<ArrayList<T>>.addNewItem(item: T) {
    val oldValue = this.value ?: arrayListOf()
    oldValue.add(item)
    this.value = oldValue
}

//add an item with different id
fun MutableList<Photo>.addWithDiffId(item: Photo) {
    this.forEach {
        if(it.id == item.id)
            return
    }
    this.add(item)
}

//liveData's extensions (MutableList version)
fun <T> MutableLiveData<ArrayList<T>>.removeItem(item: T) {
    val oldValue = this.value ?: arrayListOf()
    oldValue.remove(item)
    this.value = oldValue
}

fun <T> MutableLiveData<ArrayList<T>>.updateItem(new: T, oldPosition: Int) {
    val oldListValue = this.value ?: arrayListOf()
    oldListValue[oldPosition] = new
    this.value = oldListValue
}
