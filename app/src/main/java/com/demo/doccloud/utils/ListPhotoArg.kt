package com.demo.doccloud.utils

import android.os.Parcelable
import com.demo.doccloud.domain.entities.Photo
import kotlinx.parcelize.Parcelize

@Parcelize
data class ListPhotoArg(
    val list: ArrayList<Photo>
): Parcelable
