package com.demo.doccloud.domain.usecases.contracts

import androidx.lifecycle.LiveData
import com.demo.doccloud.domain.entities.Doc

interface GetAllDocs {
    operator fun invoke() : LiveData<List<Doc>>
}