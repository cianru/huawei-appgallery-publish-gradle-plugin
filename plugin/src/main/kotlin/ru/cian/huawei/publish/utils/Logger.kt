package ru.cian.huawei.publish.utils

import java.lang.Exception

internal class Logger {
    companion object {
        private const val LOG_TAG = "Huawei AppGallery Publishing API"

        fun i(message: String) {
            println("$LOG_TAG: $message")
        }

        fun e(exception: Exception) {
            exception.printStackTrace()
        }
    }
}
