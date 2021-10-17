package com.demo.doccloud.ui.dialogs.alert

import com.demo.doccloud.R

data class AppAlertParams(
    val title: Int,
    val message: String,
    val positiveButton: Int,
    val negativeButton: Int? = null,
    val icon: Int,
    val style: Int = R.style.ThemeOverlay_DocCloud_MaterialComponents_MaterialAlertDialog_Default,
)