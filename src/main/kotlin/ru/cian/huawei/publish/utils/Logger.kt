package ru.cian.huawei.publish.utils

private const val LOG_TAG = "Huawei AppGallery Publishing API"

class Logger {
    companion object {
        fun i(message: String) {
            println("$LOG_TAG: $message")
        }
    }
}