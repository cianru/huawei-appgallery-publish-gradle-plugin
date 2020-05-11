package ru.cian.huawei.publish.utils

import org.gradle.util.GUtil

fun String.toCamelCase() = GUtil.toCamelCase(this)

fun String?.nullIfBlank(): String? {
    if (this.isNullOrBlank()) {
        return null
    }
    return this
}