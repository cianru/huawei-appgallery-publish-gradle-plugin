package ru.cian.huawei.publish.utils

internal class Logger {
    companion object {
        const val LOG_TAG = "Huawei AppGallery Publishing API"

        fun i(message: String) {
            println("$LOG_TAG: $message")
        }
    }
}