package com.demo.doccloud.idling

class IdlingResImpl: IdlingRes {
    override suspend fun <T> wrapEspressoIdlingResource(function: suspend () -> T): T {
        EspressoIdlingResource.increment() // Set app as busy.
        return try {
            function()
        } finally {
            EspressoIdlingResource.decrement() // Set app as idle.
        }
    }
}