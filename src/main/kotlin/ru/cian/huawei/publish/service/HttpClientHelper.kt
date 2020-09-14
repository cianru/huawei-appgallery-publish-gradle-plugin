package ru.cian.huawei.publish.service

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import org.apache.http.Consts
import org.apache.http.HttpEntity
import org.apache.http.HttpStatus
import org.apache.http.client.methods.HttpDelete
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpPut
import org.apache.http.impl.client.HttpClients
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.IllegalStateException

class HttpClientHelper {

    private val gson by lazy { Gson() }

    fun <T> execute(
        httpMethod: HttpMethod,
        url: String,
        entity: HttpEntity?,
        headers: Map<String, String>?,
        clazz: Class<T>
    ): T {

        val httpRequest = when (httpMethod) {
            HttpMethod.GET -> HttpGet(url)
            HttpMethod.POST -> HttpPost(url)
            HttpMethod.PUT -> HttpPut(url)
            HttpMethod.DELETE -> HttpDelete(url)
        }

        headers?.forEach {
            httpRequest.setHeader(it.key, it.value)
        }

        if (httpRequest is HttpEntityEnclosingRequestBase) {
            httpRequest.entity = entity
        }

        try {
            val httpClient = HttpClients.createSystem()
            val httpResponse = httpClient.execute(httpRequest)
            val statusCode = httpResponse.statusLine.statusCode
            if (statusCode == HttpStatus.SC_OK) {
                val br = BufferedReader(InputStreamReader(httpResponse.entity.content, Consts.UTF_8))
                val rawResult = br.readLine()
                val result = gson.fromJson(rawResult, clazz)

                httpRequest.releaseConnection()
                httpClient.close()

                if (result == null) {
                    throw IllegalStateException("http request result must not be null")
                }

                return result
            }
            throw IllegalStateException("Request is failed. statusCode=$statusCode, httpResponse=$httpResponse")
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
        }
        throw IllegalStateException("Request is failed. Something went wrong, please check request!")
    }
}