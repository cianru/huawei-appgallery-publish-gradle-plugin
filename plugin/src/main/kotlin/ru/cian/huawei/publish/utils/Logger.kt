package ru.cian.huawei.publish.utils

import java.lang.Exception
import org.gradle.api.Project

private const val LOG_TAG = "Huawei AppGallery Publishing API"

internal object Logger {

    fun v(message: String) {
        println("$LOG_TAG: $message")
    }

    fun e(exception: Exception) {
        exception.printStackTrace()
    }

    fun i(project: Project, message: String) {
        project.logger.info("INFO, $LOG_TAG: $message")
    }
}
