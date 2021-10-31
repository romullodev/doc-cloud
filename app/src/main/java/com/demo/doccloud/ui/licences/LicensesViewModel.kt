package com.demo.doccloud.ui.licences

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demo.doccloud.R
import com.demo.doccloud.domain.entities.AppLicense
import com.demo.doccloud.domain.usecases.contracts.GetAppLicencesFromServer
import com.demo.doccloud.ui.dialogs.loading.LoadingDialogViewModel
import com.demo.doccloud.ui.login.LoginViewModel
import com.demo.doccloud.utils.Event
import com.demo.doccloud.utils.Global
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LicensesViewModel @Inject constructor(
    private val getAppLicencesUseCase: GetAppLicencesFromServer
) : ViewModel(), LoadingDialogViewModel {

    private val _appLicenses = MutableLiveData<List<AppLicense>>()
    val appLicenses: LiveData<List<AppLicense>>
        get() = _appLicenses

    //handle states
    sealed class LicensesState {
        class LicensesAlertDialog(val msg: Int) : LicensesState()
    }

    //initialize the state variable
    private val _licensesState = MutableLiveData<Event<LicensesState>>()
    val licensesState: LiveData<Event<LicensesState>>
        get() = _licensesState

    private fun syncAppLicenses() {
        showDialog(null)
        viewModelScope.launch {
            try {
                val appLicenses = getAppLicencesUseCase()
                _appLicenses.value = appLicenses
                Global.licensesCache = appLicenses
            }catch (e: Exception){
                _licensesState.value = Event(
                    LicensesState.LicensesAlertDialog(R.string.licenses_screen_error_on_load_licenses)
                )
                Timber.i(e.localizedMessage)
            }
            hideDialog()
        }
    }

    //handle loading dialog to show feedback to user (this approaches does not depend on Fragments)
    private var _loadingMessage = MutableLiveData(R.string.loading_dialog_message_please_wait)
    override val loadingMessage: LiveData<Int> get() = _loadingMessage
    private var _isDialogVisible = MutableLiveData(false)
    override val isDialogVisible: LiveData<Boolean> get() = _isDialogVisible

    override fun showDialog(message: Int?) {
        message?.let {
            _loadingMessage.value = message
        }
        _isDialogVisible.value = true
    }

    override fun hideDialog() {
        _isDialogVisible.value = false
    }

    fun syncLicenses(){
        if(Global.licensesCache.isEmpty()){
            syncAppLicenses()
        }else{
            _appLicenses.value = Global.licensesCache
        }
    }
}