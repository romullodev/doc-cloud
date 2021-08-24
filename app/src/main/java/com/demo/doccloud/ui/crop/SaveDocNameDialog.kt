package com.demo.doccloud.ui.crop

import android.app.Dialog
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.demo.doccloud.R
import com.demo.doccloud.databinding.SaveDocNameLayoutBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SaveDocNameDialog : DialogFragment() {

    companion object {
        private const val DIALOG_DOC_INTERFACE_KEY = "dialog.doc.interface.key"
        internal fun newInstance(
            listener: DialogDocNameListener,
        ) = SaveDocNameDialog().apply {
            arguments = Bundle().apply {
                putParcelable(DIALOG_DOC_INTERFACE_KEY, listener)
            }
        }
    }

    interface DialogDocNameListener : Parcelable {
        fun onSaveClick(docName: String, dialog: DialogFragment)
        fun onCancelClick(dialog: DialogFragment)
        override fun describeContents(): Int = 0
        override fun writeToParcel(dest: Parcel, flags: Int) { /* nop */
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val listener : DialogDocNameListener = arguments?.getParcelable(DIALOG_DOC_INTERFACE_KEY)!!
        return activity?.let {
            val builder =
                MaterialAlertDialogBuilder(it, R.style.ThemeOverlay_App_MaterialAlertDialog)
            val layoutInflater = LayoutInflater.from(context)
            val binding = SaveDocNameLayoutBinding.inflate(layoutInflater, null, false)
            binding.cancelBtn.setOnClickListener {
                listener.onCancelClick(this)
            }
            binding.saveBtn.setOnClickListener {
                listener.onSaveClick(binding.inputName.text.toString(), this)
            }

            val localDialog = builder.create()
            localDialog.setView(binding.root)
            localDialog.setCanceledOnTouchOutside(false)
            localDialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}