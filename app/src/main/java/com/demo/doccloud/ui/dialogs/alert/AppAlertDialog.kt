package com.demo.doccloud.ui.dialogs.alert

import android.app.Dialog
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson


class AppAlertDialog : DialogFragment() {
    companion object {
        private const val DIALOG_PARAMETER_KEY = "dialog.parameter.key"
        private const val DIALOG_INTERFACE_KEY = "dialog.interface.key"
        internal fun newInstance(
            params: AppAlertParams,
            listener: DialogMaterialListener,
        ) = AppAlertDialog().apply {
            this.arguments = Bundle().apply {
                this.putString(DIALOG_PARAMETER_KEY, Gson().toJson(params))
                this.putParcelable(DIALOG_INTERFACE_KEY, listener)
            }
        }
    }

    interface DialogMaterialListener : Parcelable {
        fun onDialogPositiveClick(dialog: DialogFragment)
        fun onDialogNegativeClick(dialog: DialogFragment)
        override fun describeContents(): Int = 0
        override fun writeToParcel(dest: Parcel, flags: Int) {}
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val params: AppAlertParams =  Gson().fromJson(arguments?.getString(DIALOG_PARAMETER_KEY), AppAlertParams::class.java)
        val listener : DialogMaterialListener? = arguments?.getParcelable(DIALOG_INTERFACE_KEY)
        return activity?.let {
            val builder = MaterialAlertDialogBuilder(it, params.style)
            builder
                .setTitle(params.title)
                .setMessage(params.message)
                .setIcon(params.icon)
                .setPositiveButton(params.positiveButton) { _, _ ->
                    listener?.onDialogPositiveClick(this)
                }
            params.negativeButton?.let { negativeBtn ->
                builder.setNegativeButton(negativeBtn) { _, _ ->
                    listener?.onDialogNegativeClick(this)
                }
            }

            val dialog = builder.create()
            dialog.setCanceledOnTouchOutside(false)
            dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}