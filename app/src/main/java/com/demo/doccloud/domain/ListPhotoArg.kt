package com.demo.doccloud.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ListPhotoArg(
    val list: ArrayList<Photo>
): Parcelable
