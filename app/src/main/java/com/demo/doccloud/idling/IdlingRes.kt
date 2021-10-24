package com.demo.doccloud.idling

interface IdlingRes {
    suspend fun<T> wrapEspressoIdlingResource(function: suspend () -> T): T
}