package ru.cian.huawei.publish.service.mock

interface MockServerWrapper {

    fun getBaseUrl(): String

    fun start()

    fun shutdown()
}