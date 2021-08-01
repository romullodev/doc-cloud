package com.demo.doccloud.ui.dialogs.alert

import com.demo.doccloud.R

data class DefaultAlertParams (
    val title: Int = R.string.alert_dialog_default_tittle,
    val message: String,
    val positiveButton: Int = R.string.alert_dialog_default_ok_button,
    val icon: Int = android.R.drawable.ic_dialog_alert,
    val style: Int = R.style.ThemeOverlay_DocCloud_MaterialComponents_MaterialAlertDialog_Default,
)