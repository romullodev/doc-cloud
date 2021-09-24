package com.demo.doccloud

import java.io.File

object GlobalVariablesTest{
    var shouldThrowException: Boolean = false
    val fakeFile: File = File("any")
    const val delayDuration = 1000L
    var hasDelay = false

    fun clearFlags(){
        hasDelay = false
        shouldThrowException = false
    }
}