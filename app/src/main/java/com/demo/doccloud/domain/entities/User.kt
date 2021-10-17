package com.demo.doccloud.domain.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val displayName: String,
    val userId: String,
): Parcelable