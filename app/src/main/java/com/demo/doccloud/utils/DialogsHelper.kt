package com.demo.doccloud.utils

import androidx.fragment.app.FragmentActivity
import com.demo.doccloud.R
import com.demo.doccloud.ui.dialogs.alert.AppAlertDialog
import com.demo.doccloud.ui.dialogs.alert.AppAlertParams

object DialogsHelper {

    fun getQuestionDeleteAlertParams(msg: String) = AppAlertParams(
        title = R.string.alert_dialog_question_delete_tittle,
        message = msg,
        positiveButton = R.string.alert_dialog_yes_button,
        negativeButton = R.string.alert_dialog_No_button,
        icon = R.drawable.ic_delete_24
    )

    fun getInfoAlertParams(msg: String) = AppAlertParams(
        title = R.string.alert_dialog_info_tittle,
        message = msg,
        positiveButton = R.string.alert_dialog_info_ok_button,
        icon = R.drawable.ic_baseline_info_24
    )


    fun showAlertDialog(
        params: AppAlertParams,
        listener: AppAlertDialog.DialogMaterialListener,
        fragAct: FragmentActivity,
        tag: String? = null
    ) {
        val materialDialog = AppAlertDialog.newInstance(params, listener)//(params, listener)

        materialDialog.show(
            fragAct.supportFragmentManager,
            tag
        )
    }

}