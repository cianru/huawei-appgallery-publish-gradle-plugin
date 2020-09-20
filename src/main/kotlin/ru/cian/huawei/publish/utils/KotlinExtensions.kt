package ru.cian.huawei.publish.utils

import org.gradle.util.GUtil

internal fun String.toCamelCase() = GUtil.toCamelCase(this)

internal fun String?.nullIfBlank(): String? {
    if (this.isNullOrBlank()) {
        return null
    }
    return this
}