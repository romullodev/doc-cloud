package com.demo.doccloud.domain.usecases.impl

import com.demo.doccloud.data.repository.Repository
import com.demo.doccloud.domain.entities.AppLicense
import com.demo.doccloud.domain.usecases.contracts.GetAppLicencesFromServer
import javax.inject.Inject

class GetAppLicencesFromServerImpl @Inject constructor(
    private val repository: Repository
): GetAppLicencesFromServer {
    override suspend fun invoke(): List<AppLicense>  = repository.getAppLicences()
}