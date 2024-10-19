package ru.cian.huawei.publish.utils

import java.lang.Exception
import org.gradle.api.Project

private const val LOG_TAG = "Huawei AppGallery Publishing API"

class Logger constructor(
    private val project: Project
) {

    fun v(message: String) {
        println("$LOG_TAG: $message")
    }

    fun e(exception: Exception) {
        exception.printStackTrace()
    }

    fun i(message: String) {
        project.logger.info("INFO, $LOG_TAG: $message")
    }
}
