package com.demo.doccloud.modules.login.domain.errors

abstract class LoginErrors : Exception()

class LoginNullError(val msg: String): LoginErrors()
class UseCaseError(val msg: String): LoginErrors()