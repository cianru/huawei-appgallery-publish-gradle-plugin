package ru.cian.huawei.publish.service.mock

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import ru.cian.huawei.publish.utils.Logger
import java.util.concurrent.TimeUnit

private const val DELAY_REQUEST_BODY_SECONDS = 1L

@SuppressWarnings("MaxLineLength", "MagicNumber")
class MockServerWrapperImpl(
    val logger: Logger,
): MockServerWrapper {

    private lateinit var mockWebServer: MockWebServer

    override fun getBaseUrl(): String {
        return mockWebServer.url("/").toString()
    }

    override fun start() {
        logger.v(":: start mock server")

        mockWebServer = MockWebServer()
        mockWebServer.start()

        val dispatcher = object : Dispatcher() {
            @Throws(InterruptedException::class)
            @SuppressWarnings("ReturnCount")
            override fun dispatch(request: RecordedRequest): MockResponse {
                when {
                    request.path!!.contains("/oauth2/v1/token") -> return MockResponse()
                        .setResponseCode(200)
                        .setBodyDelay(DELAY_REQUEST_BODY_SECONDS, TimeUnit.SECONDS)
                        .setBody(
                            """{
                                  "access_token": "abcd1234token",
                                  "expires_in": 3600,
                                  "ret": {
                                    "code": 0,
                                    "msg": "Success"
                                  }
                            }""".trimMargin())
                    request.path!!.contains("/appid-list") -> return MockResponse()
                        .setResponseCode(200)
                        .setBodyDelay(DELAY_REQUEST_BODY_SECONDS, TimeUnit.SECONDS)
                        .setBody(
                            """{
                                  "ret": {
                                    "code": 0,
                                    "msg": "Success"
                                  },
                                  "appids": [
                                    {
                                      "key": "app1",
                                      "value": "App One"
                                    },
                                    {
                                      "key": "app2",
                                      "value": "App Two"
                                    }
                                  ]
                            }""".trimMargin())
                    request.path!!.contains("/upload-url") -> return MockResponse()
                        .setResponseCode(200)
                        .setBodyDelay(DELAY_REQUEST_BODY_SECONDS, TimeUnit.SECONDS)
                        .setBody(
                            """{
                                  "ret": {
                                    "code": 0,
                                    "msg": "Success"
                                  },
                                  "uploadUrl": "${getBaseUrl()}/upload_file_stub",
                                  "chunkUploadUrl": "${getBaseUrl()}/upload/chunk_stub",
                                  "authCode": "abc123securetoken"
                                }
                            """.trimMargin())
                    request.path!!.contains("/upload_file_stub") -> return MockResponse()
                        .setResponseCode(200)
                        .setBodyDelay(DELAY_REQUEST_BODY_SECONDS, TimeUnit.SECONDS)
                        .setBody(
                            """{
                              "result": {
                                "resultCode": 0,
                                "UploadFileRsp": {
                                  "ifSuccess": 1,
                                  "fileInfoList": [
                                    {
                                      "fileDestUlr": "https://example.com/files/file1.jpg",
                                      "imageResolution": "1920x1080",
                                      "imageResolutionSingature": "signature123",
                                      "size": 2048
                                    },
                                    {
                                      "fileDestUlr": "https://example.com/files/file2.jpg",
                                      "imageResolution": "1280x720",
                                      "imageResolutionSingature": "signature456",
                                      "size": 1024
                                    }
                                  ]
                                }
                              }
                            }""".trimMargin())
                    request.path!!.contains("/app-file-info") -> return MockResponse()
                        .setResponseCode(200)
                        .setBodyDelay(DELAY_REQUEST_BODY_SECONDS, TimeUnit.SECONDS)
                        .setBody(
                            """{
                                  "ret": {
                                    "code": 0,
                                    "msg": "Success"
                                  }
                            }""".trimMargin())
                    request.path!!.contains("/app-language-info") -> return MockResponse()
                        .setResponseCode(200)
                        .setBodyDelay(DELAY_REQUEST_BODY_SECONDS, TimeUnit.SECONDS)
                        .setBody(
                            """{
                                  "ret": {
                                    "code": 0,
                                    "msg": "Success"
                                  }
                            }""".trimMargin())
                    request.path!!.contains("/app-submit") -> return MockResponse()
                        .setResponseCode(200)
                        .setBodyDelay(DELAY_REQUEST_BODY_SECONDS, TimeUnit.SECONDS)
                        .setBody(
                            """{
                                  "ret": {
                                    "code": 0,
                                    "msg": "Success"
                                  }
                            }""".trimMargin())
                    request.path!!.contains("/app-info") -> return MockResponse()
                        .setResponseCode(200)
                        .setBodyDelay(DELAY_REQUEST_BODY_SECONDS, TimeUnit.SECONDS)
                        .setBody(
                            """{
                                  "ret": {
                                    "code": 0,
                                    "msg": "Success"
                                  }
                            }""".trimMargin())
                }
                return MockResponse().setResponseCode(404)
            }
        }
        mockWebServer.dispatcher = dispatcher
    }

    override fun shutdown() {
        logger.v(":: shutdown mock server")
        mockWebServer.shutdown()
    }
}