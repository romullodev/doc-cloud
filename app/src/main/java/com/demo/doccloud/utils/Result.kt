package com.demo.doccloud.utils

data class Result<out T>(val status: Status, val data: T?, val msg: String?) {

    enum class Status {
        SUCCESS,
        ERROR
    }

    companion object {
        fun <T> success(data: T): Result<T> {
            return Result(Status.SUCCESS, data, null)
        }

        fun <T> error(msg: String, data: T? = null): Result<T> {
            return Result(Status.ERROR, data, msg)
        }
    }
}