package com.demo.doccloud.utils

import androidx.fragment.app.FragmentActivity
import com.demo.doccloud.ui.dialogs.alert.DefaultAlertDialog
import com.demo.doccloud.ui.dialogs.alert.DefaultAlertParams

object DialogsHelper {

    fun showAlertDialog(
        params: DefaultAlertParams,
        listener: DefaultAlertDialog.DialogMaterialListener,
        fragAct: FragmentActivity,
        tag: String? = null
    ) {
        val materialDialog = DefaultAlertDialog.newInstance(params, listener)//(params, listener)

        materialDialog.show(
            fragAct.supportFragmentManager,
            tag
        )
    }

}