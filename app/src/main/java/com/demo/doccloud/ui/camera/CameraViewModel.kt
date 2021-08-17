package com.demo.doccloud.ui.camera

import androidx.lifecycle.ViewModel
import com.demo.doccloud.data.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel(){

}