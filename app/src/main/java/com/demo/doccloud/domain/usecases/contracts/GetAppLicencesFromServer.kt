package com.demo.doccloud.domain.usecases.contracts

import com.demo.doccloud.domain.entities.AppLicense

interface GetAppLicencesFromServer {
    suspend operator fun invoke() : List<AppLicense>
}