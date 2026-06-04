package ru.cian.huawei.publish.utils

import java.lang.Exception
import org.gradle.api.logging.Logger as GradleLogger

private const val LOG_TAG = "Huawei AppGallery Publishing API"

class Logger constructor(
    private val gradleLogger: GradleLogger
) {

    fun v(message: String) {
        println("$LOG_TAG: $message")
    }

    fun e(exception: Exception) {
        exception.printStackTrace()
    }

    fun i(message: String) {
        gradleLogger.info("INFO, $LOG_TAG: $message")
    }
}
