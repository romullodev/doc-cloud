package com.demo.doccloud.idling

class IdlingResImpl: IdlingRes {
    override suspend fun <T> wrapEspressoIdlingResource(function: suspend () -> T): T {
        // for Release, do nothing
        return function()
    }
}