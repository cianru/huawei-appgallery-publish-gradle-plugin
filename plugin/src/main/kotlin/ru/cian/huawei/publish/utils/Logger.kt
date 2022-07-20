package ru.cian.huawei.publish.utils

import org.gradle.api.Project

private const val LOG_TAG = "Huawei AppGallery Publishing API"

internal class Logger {
    companion object {
        fun i(message: String) {
            println("$LOG_TAG: $message")
        }

        fun d(project: Project, message: String) {
            project.logger.info("$LOG_TAG: $message")
        }
    }
}