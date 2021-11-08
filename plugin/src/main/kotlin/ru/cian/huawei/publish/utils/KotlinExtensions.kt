package ru.cian.huawei.publish.utils

import org.gradle.util.GUtil
import java.lang.StringBuilder

private const val SECOND_IN_MILLIS = 1000L
private const val MINUTE_IN_MILLIS = SECOND_IN_MILLIS * 60
private const val HOUR_IN_MILLIS = MINUTE_IN_MILLIS * 60
private const val DAY_IN_MILLIS = HOUR_IN_MILLIS * 24
private const val WEEK_IN_MILLIS = DAY_IN_MILLIS * 7

internal fun String.toCamelCase() = GUtil.toCamelCase(this)

internal fun String?.nullIfBlank(): String? {
    if (this.isNullOrBlank()) {
        return null
    }
    return this
}

internal fun Long.toHumanPrettyFormatInterval(): String {

    if (this < SECOND_IN_MILLIS) {
        return "0s"
    }

    var interval = this
    val weeks = interval / WEEK_IN_MILLIS
    interval -= weeks * WEEK_IN_MILLIS
    val days = interval / DAY_IN_MILLIS
    interval -= days * DAY_IN_MILLIS
    val hours = interval / HOUR_IN_MILLIS
    interval -= hours * HOUR_IN_MILLIS
    val minutes = interval / MINUTE_IN_MILLIS
    interval -= minutes * MINUTE_IN_MILLIS
    val seconds = interval / SECOND_IN_MILLIS
    return StringBuilder()
        .append(if (weeks > 0) "${weeks}w" else "")
        .append(if (days > 0) "${days}d" else "")
        .append(if (hours > 0) "${hours}h" else "")
        .append(if (minutes > 0) "${minutes}m" else "")
        .append(if (seconds > 0) "${seconds}s" else "")
        .toString()
}
