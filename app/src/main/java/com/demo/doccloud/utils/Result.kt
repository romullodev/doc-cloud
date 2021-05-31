package com.demo.doccloud.utils

data class Result<out T>(val status: Status, val data: T?, val exception: Exception?) {

    enum class Status {
        SUCCESS,
        ERROR
    }

    companion object {
        fun <T> success(data: T): Result<T> {
            return Result(Status.SUCCESS, data, null)
        }

        fun <T> error(exception: Exception, data: T? = null): Result<T> {
            return Result(Status.ERROR, data, exception)
        }
    }
}