package com.demo.doccloud

import java.io.File

object GlobalVariablesTest{
    var shouldThrowException: Boolean = false
    val fakeFile: File = File("any")
}