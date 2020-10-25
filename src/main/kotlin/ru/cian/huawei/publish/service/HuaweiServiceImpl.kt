package ru.cian.huawei.publish.service

import com.google.gson.Gson
import org.apache.http.client.HttpResponseException
import org.apache.http.client.utils.URIBuilder
import org.apache.http.entity.StringEntity
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.entity.mime.content.FileBody
import ru.cian.huawei.publish.models.HuaweiHttpResponseException
import ru.cian.huawei.publish.models.request.AccessTokenRequest
import ru.cian.huawei.publish.models.request.FileInfoRequest
import ru.cian.huawei.publish.models.request.PhasedReleaseRequest
import ru.cian.huawei.publish.models.request.UpdateAppFileInfoRequest
import ru.cian.huawei.publish.models.response.AccessTokenResponse
import ru.cian.huawei.publish.models.response.AppIdResponse
import ru.cian.huawei.publish.models.response.AppInfo
import ru.cian.huawei.publish.models.response.FileServerOriResultResponse
import ru.cian.huawei.publish.models.response.SubmitResponse
import ru.cian.huawei.publish.models.response.UpdateAppFileInfoResponse
import ru.cian.huawei.publish.models.response.UploadUrlResponse
import java.io.File
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
        val entity = getEntity(body)

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

    override fun getUploadingBuildUrl(
        clientId: String,
        token: String,
        appId: String,
        suffix: String
    ): UploadUrlResponse {

        val headers = mutableMapOf<String, String>()
        headers["Authorization"] = "Bearer $token"
        headers["client_id"] = clientId

        return httpClient.execute(
            httpMethod = HttpMethod.GET,
            url = "$PUBLISH_API_URL/upload-url?appId=$appId&suffix=$suffix",
            entity = null,
            headers = headers,
            clazz = UploadUrlResponse::class.java
        )
    }

    override fun uploadBuildFile(
        uploadUrl: String,
        authCode: String,
        buildFile: File
    ): FileServerOriResultResponse {

        val fileBody = FileBody(buildFile)
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
            throw IllegalStateException("Build file uploading is failed!")
        }

        return result
    }

    override fun updateAppFileInformation(
        clientId: String,
        token: String,
        appId: String,
        releaseType: Int,
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
        val entity = getEntity(body)

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

    override fun submitReviewImmediately(
        clientId: String,
        token: String,
        appId: String,
        releaseTime: String?
    ): SubmitResponse {
        return submitReview(
            clientId = clientId,
            token = token,
            appId = appId,
            releaseType = 1,
            releaseTime = releaseTime,
            entity = null
        )
    }

    override fun submitReviewWithReleasePhase(
        clientId: String,
        token: String,
        appId: String,
        startRelease: String?,
        endRelease: String?,
        releasePercent: Double
    ): SubmitResponse {
        val bodyRequest = PhasedReleaseRequest(
            phasedReleaseStartTime = startRelease!!,
            phasedReleaseEndTime = endRelease!!,
            phasedReleasePercent = "%.2f".format(releasePercent),
            phasedReleaseDescription = "Release on $releasePercent% from $startRelease to $endRelease"
        )
        val body = gson.toJson(bodyRequest)
        val entity = getEntity(body)

        return submitReview(
            clientId = clientId,
            token = token,
            appId = appId,
            releaseType = 3,
            releaseTime = null,
            entity = entity
        )
    }

    private fun submitReview(
        clientId: String,
        token: String,
        appId: String,
        releaseType: Int,
        releaseTime: String?,
        entity: StringEntity?
    ): SubmitResponse {
        val headers = mutableMapOf<String, String>()
        headers["Content-Type"] = "application/json"
        headers["Accept"] = "application/json"
        headers["Authorization"] = "Bearer $token"
        headers["client_id"] = clientId

        val uriBuilder = URIBuilder("$PUBLISH_API_URL/app-submit")
            .addParameter("appId", appId)
            .addParameter("releaseType", releaseType.toString())
        if (releaseTime != null) {
            uriBuilder.addParameter("releaseTime", releaseTime)
        }
        val url = uriBuilder.build().toURL().toString()

        val result = httpClient.execute(
            httpMethod = HttpMethod.POST,
            url = url,
            entity = entity,
            headers = headers,
            clazz = SubmitResponse::class.java
        )

        if (result.ret.code != 0) {
            throw HuaweiHttpResponseException(result.toString())
        }

        return result
    }

    private fun getEntity(body: String?): StringEntity {
        val entity = StringEntity(body, Charset.forName("UTF-8"))
        entity.setContentEncoding("UTF-8")
        entity.setContentType("application/json")
        return entity
    }
}