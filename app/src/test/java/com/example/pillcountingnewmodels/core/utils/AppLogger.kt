package com.example.pillcountingnewmodels.core.utils

class AppLogger(private val tag: String) {

    fun d(message: String, throwable: Throwable? = null) = println("DEBUG: [$tag] $message")
    fun i(message: String, throwable: Throwable? = null) = println("INFO: [$tag] $message")
    fun w(message: String, throwable: Throwable? = null) = println("WARN: [$tag] $message")
    fun e(message: String, throwable: Throwable? = null) = println("ERROR: [$tag] $message ${throwable?.message ?: ""}")

    companion object {
        inline fun <reified T> create(): AppLogger = AppLogger(T::class.java.simpleName)
    }
}