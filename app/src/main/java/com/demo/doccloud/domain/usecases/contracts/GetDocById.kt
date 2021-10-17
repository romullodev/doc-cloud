package com.demo.doccloud.domain.usecases.contracts

import com.demo.doccloud.domain.entities.Doc

interface GetDocById {
    suspend operator fun invoke(id: Long) : Doc
}