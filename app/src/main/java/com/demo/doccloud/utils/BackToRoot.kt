package com.demo.doccloud.utils

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BackToRoot(
    val rootDestination: RootDestination,
    val localId: Long? = null// in case of edit_destination
) : Parcelable

enum class RootDestination {
    HOME_DESTINATION, EDIT_DESTINATION
}

