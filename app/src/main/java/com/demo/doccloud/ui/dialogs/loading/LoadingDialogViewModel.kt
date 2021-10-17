package com.demo.doccloud.ui.dialogs.loading

import androidx.lifecycle.LiveData

//this interface forces to implement the required params to use loadingDialog correctly
interface LoadingDialogViewModel {

    val loadingMessage: LiveData<Int>
    val isDialogVisible: LiveData<Boolean>

    fun showDialog(message: Int?)
    fun hideDialog()
}