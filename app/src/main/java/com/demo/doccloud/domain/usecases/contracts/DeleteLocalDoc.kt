package com.demo.doccloud.domain.usecases.contracts

import com.demo.doccloud.domain.entities.Doc

interface DeleteLocalDoc {
    suspend operator fun invoke(doc: Doc)
}