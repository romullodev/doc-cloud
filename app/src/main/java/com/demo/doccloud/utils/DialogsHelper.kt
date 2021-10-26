package com.demo.doccloud.utils

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import androidx.fragment.app.FragmentActivity
import com.demo.doccloud.R
import com.demo.doccloud.databinding.GeneratePdfLayoutBinding
import com.demo.doccloud.databinding.HomeDialogNewDocBinding
import com.demo.doccloud.ui.dialogs.alert.AppAlertDialog
import com.demo.doccloud.ui.dialogs.alert.AppAlertParams
import com.demo.doccloud.ui.home.HomeFragmentDirections
import com.google.android.material.dialog.MaterialAlertDialogBuilder

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

    fun showGeneratePdfOrLinkDialog(context: Context, runOnPdfFile: Runnable, runCodeOnPdfLink: Runnable){
        val dialog = MaterialAlertDialogBuilder(
            context,
            R.style.ThemeOverlay_App_MaterialAlertDialog
        ).create()
        val layoutInflater = LayoutInflater.from(context)
        val view = GeneratePdfLayoutBinding.inflate(layoutInflater, null, false)
        view.btnClose.setOnClickListener {
            dialog.dismiss()
        }
        view.sharePdfFileTv.setOnClickListener {
            runOnPdfFile.run()
            dialog.dismiss()
        }
        view.sharePdfLinkTv.setOnClickListener {
            runCodeOnPdfLink.run()
            dialog.dismiss()
        }
        dialog.setView(view.root)
        dialog.show()
    }

    fun showAddDocDialog(context: Context, runOnCamera: Runnable, runCodeOnGallery: Runnable){
        val dialog = MaterialAlertDialogBuilder(
            context,
            R.style.ThemeOverlay_App_MaterialAlertDialog
        ).create()
        val layoutInflater = LayoutInflater.from(context)
        val view = HomeDialogNewDocBinding.inflate(layoutInflater, null, false)
        view.btnClose.setOnClickListener {
            dialog.dismiss()
        }
        view.cameraTv.setOnClickListener {
            runOnCamera.run()
            dialog.dismiss()
        }
        view.galleryTv.setOnClickListener {
            runCodeOnGallery.run()
            dialog.dismiss()
        }
        dialog.setView(view.root)
        dialog.show()
    }


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