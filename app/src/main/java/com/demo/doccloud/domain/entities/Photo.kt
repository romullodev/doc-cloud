package com.demo.doccloud.domain.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

//CropFragment needs a list of Photo passed in as args
@Parcelize
data class Photo(
    val id: Long,
    val path: String,
): Parcelable