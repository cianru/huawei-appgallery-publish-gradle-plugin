package ru.cian.huawei.publish.utils

import java.util.concurrent.TimeoutException

internal class ServerPollingExecutor {

    @Suppress("TooGenericExceptionCaught")
    fun run(
        periodTimeInMs: Long,
        timeoutInMs: Long,
        action: (() -> Unit),
        processListener: ((timeLeft: Long, exception: Exception) -> Unit),
        successListener: (() -> Unit),
        failListener: ((lastError: Exception?) -> Unit)
    ) {
        val timeoutOutMoment = getCurrentTimestampInMs() + timeoutInMs
        var isFinished = false
        var lastException: Exception? = null
        while (!isFinished && getCurrentTimestampInMs() < timeoutOutMoment) {
            try {
                action.invoke()
                isFinished = true
                successListener.invoke()
            } catch (exception: Exception) {
                lastException = exception
                val timeLeft = Math.max(timeoutOutMoment - getCurrentTimestampInMs(), 0)
                processListener(timeLeft, exception)
                Thread.sleep(Math.min(periodTimeInMs, timeLeft))
            }
        }

        if (!isFinished) {
            failListener.invoke(TimeoutException(lastException.toString()))
        }
    }

    private fun getCurrentTimestampInMs() = System.currentTimeMillis()
}
