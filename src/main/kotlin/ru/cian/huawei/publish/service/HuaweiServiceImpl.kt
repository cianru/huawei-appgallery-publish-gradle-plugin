package ru.cian.huawei.publish.service

import com.google.gson.Gson
import org.apache.http.entity.StringEntity
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.entity.mime.content.FileBody
import ru.cian.huawei.publish.models.request.AccessTokenRequest
import ru.cian.huawei.publish.models.request.FileInfoRequest
import ru.cian.huawei.publish.models.request.UpdateAppFileInfoRequest
import ru.cian.huawei.publish.models.response.AccessTokenResponse
import ru.cian.huawei.publish.models.response.AppIdResponse
import ru.cian.huawei.publish.models.response.AppInfo
import ru.cian.huawei.publish.models.response.FileServerOriResultResponse
import ru.cian.huawei.publish.models.response.SubmitResponse
import ru.cian.huawei.publish.models.response.UpdateAppFileInfoResponse
import ru.cian.huawei.publish.models.response.UploadUrlResponse
import java.io.File
import java.lang.IllegalStateException
import java.nio.charset.Charset

private const val DOMAIN_URL = "https://connect-api.cloud.huawei.com/api"
private const val PUBLISH_API_URL = "$DOMAIN_URL/publish/v2"
private const val GRANT_TYPE = "client_credentials"

internal class HuaweiServiceImpl : HuaweiService {

    private val gson = Gson()
    private val httpClient = HttpClientHelper()

    override fun getToken(
        clientId: String,
        clientSecret: String
    ): String {

        val bodyRequest = AccessTokenRequest(
            clientId = clientId,
            clientSecret = clientSecret,
            grantType = GRANT_TYPE
        )
        val body = gson.toJson(bodyRequest)

        val entity = StringEntity(body, Charset.forName("UTF-8"))
        entity.setContentEncoding("UTF-8")
        entity.setContentType("application/json")

        val accessTokenResponse = httpClient.execute(
            httpMethod = HttpMethod.POST,
            url = "$DOMAIN_URL/oauth2/v1/token",
            entity = entity,
            headers = null,
            clazz = AccessTokenResponse::class.java
        )

        return accessTokenResponse.accessToken
    }

    override fun getAppID(
        clientId: String,
        token: String,
        packageName: String
    ): AppInfo {

        val headers = mutableMapOf<String, String>()
        headers["Authorization"] = "Bearer $token"
        headers["client_id"] = clientId

        val appIdResponse = httpClient.execute(
            httpMethod = HttpMethod.GET,
            url = "$PUBLISH_API_URL/appid-list?packageName=$packageName",
            entity = null,
            headers = headers,
            clazz = AppIdResponse::class.java
        )
        if (appIdResponse.appids.isEmpty()) {
            throw IllegalStateException("`appids` must not be empty")
        }
        return appIdResponse.appids[0]
    }

    override fun getUploadApkUrl(
        clientId: String,
        token: String,
        appId: String
    ): UploadUrlResponse {

        val headers = mutableMapOf<String, String>()
        headers["Authorization"] = "Bearer $token"
        headers["client_id"] = clientId

        return httpClient.execute(
            httpMethod = HttpMethod.GET,
            url = "$PUBLISH_API_URL/upload-url?appId=$appId&suffix=apk",
            entity = null,
            headers = headers,
            clazz = UploadUrlResponse::class.java
        )
    }

    override fun uploadApkFile(
        uploadUrl: String,
        authCode: String,
        apkFile: File
    ): FileServerOriResultResponse {

        val fileBody = FileBody(apkFile)
        val entity = MultipartEntityBuilder.create()
            .addPart("file", fileBody)
            .addTextBody("authCode", authCode)
            .addTextBody("fileCount", "1")
            .addTextBody("parseType", "1")
            .build()

        val headers = mutableMapOf<String, String>()
        headers["accept"] = "application/json"

        val result = httpClient.execute(
            httpMethod = HttpMethod.POST,
            url = uploadUrl,
            entity = entity,
            headers = headers,
            clazz = FileServerOriResultResponse::class.java
        )

        if (result.result.resultCode != 0) {
            throw IllegalStateException("APK file uploading is failed!")
        }

        return result
    }

    override fun updateAppFileInformation(
        clientId: String,
        token: String,
        appId: String,
        fileInfoRequestList: List<FileInfoRequest>
    ): UpdateAppFileInfoResponse {

        val headers = mutableMapOf<String, String>()
        headers["Authorization"] = "Bearer $token"
        headers["client_id"] = clientId

        val bodyRequest = UpdateAppFileInfoRequest(
            fileType = 5,
            files = fileInfoRequestList
        )
        val body = gson.toJson(bodyRequest)

        val entity = StringEntity(body, Charset.forName("UTF-8"))
        entity.setContentEncoding("UTF-8")
        entity.setContentType("application/json")

        val result = httpClient.execute(
            httpMethod = HttpMethod.PUT,
            url = "$PUBLISH_API_URL/app-file-info?appId=$appId",
            entity = entity,
            headers = headers,
            clazz = UpdateAppFileInfoResponse::class.java
        )

        if (result.ret.code != 0) {
            throw IllegalStateException("Update App File Info is failed. Response: $result")
        }

        return result
    }

    override fun submitPublication(
        clientId: String,
        token: String,
        appId: String
    ): SubmitResponse {
        val headers = mutableMapOf<String, String>()
        headers["Authorization"] = "Bearer $token"
        headers["client_id"] = clientId

        val result = httpClient.execute(
            httpMethod = HttpMethod.POST,
            url = "$PUBLISH_API_URL/app-submit?appId=$appId",
            entity = null,
            headers = headers,
            clazz = SubmitResponse::class.java
        )

        if (result.ret.code != 0) {
            throw IllegalStateException(result.ret.toString())
        }

        return result
    }
}