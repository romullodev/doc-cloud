package com.demo.doccloud.ui.dialogs.loading

import  android.app.Dialog
import android.content.Context
import androidx.annotation.StyleRes
import com.demo.doccloud.R

class CustomDialog(context: Context, @StyleRes dialogStyle: Int) : Dialog(context, dialogStyle) {
    init {
        // Set Semi-Transparent Color for Dialog Background
        //window?.decorView?.rootView?.setBackgroundResource(R.color.a_60_black)
        this.window?.setBackgroundDrawableResource(R.color.a_60_black)
        //@Suppress("DEPRECATION")
        //window?.decorView?.setOnApplyWindowInsetsListener { _, insets ->
        //    insets.consumeSystemWindowInsets()
        //}
    }
}