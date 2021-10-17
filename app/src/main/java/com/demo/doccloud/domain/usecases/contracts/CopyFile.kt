package com.demo.doccloud.domain.usecases.contracts

import android.net.Uri
import java.io.File

interface CopyFile {
    suspend operator fun invoke(uri: Uri) : File?
}