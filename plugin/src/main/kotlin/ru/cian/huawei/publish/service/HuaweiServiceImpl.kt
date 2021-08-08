package ru.cian.huawei.publish.service

import com.google.gson.Gson
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
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
import ru.cian.huawei.publish.service.HttpClientHelper.Companion.MEDIA_TYPE_AAB
import ru.cian.huawei.publish.service.HttpClientHelper.Companion.MEDIA_TYPE_JSON
import ru.cian.huawei.publish.utils.Logger
import java.io.File

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

        val accessTokenResponse = httpClient.post<AccessTokenResponse>(
            url = "$DOMAIN_URL/oauth2/v1/token",
            body = gson.toJson(bodyRequest).toRequestBody(MEDIA_TYPE_JSON),
            headers = null,
        )
        return accessTokenResponse.accessToken
            ?: throw IllegalStateException("Can't get `accessToken`. Reason: '${accessTokenResponse.ret}'")
    }

    override fun getAppID(
        clientId: String,
        token: String,
        packageName: String
    ): AppInfo {

        val headers = mutableMapOf<String, String>()
        headers["Authorization"] = "Bearer $token"
        headers["client_id"] = clientId

        val appIdResponse = httpClient.get<AppIdResponse>(
            url = "$PUBLISH_API_URL/appid-list?packageName=$packageName",
            headers = headers,
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

        return httpClient.get(
            url = "$PUBLISH_API_URL/upload-url?appId=$appId&suffix=$suffix",
            headers = headers
        )
    }

    override fun uploadBuildFile(
        uploadUrl: String,
        authCode: String,
        buildFile: File
    ): FileServerOriResultResponse {

        val fileBody = buildFile.asRequestBody(MEDIA_TYPE_AAB)
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", buildFile.name, fileBody)
            .addFormDataPart("authCode", authCode)
            .addFormDataPart("fileCount", "1")
            .addFormDataPart("parseType", "1")
            .build()

        val headers = mutableMapOf<String, String>()
        headers["accept"] = "application/json"

        val result = httpClient.post<FileServerOriResultResponse>(
            url = uploadUrl,
            body = requestBody,
            headers = headers
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

        val result = httpClient.put<UpdateAppFileInfoResponse>(
            url = "$PUBLISH_API_URL/app-file-info?appId=$appId&releaseType=$releaseType",
            body = gson.toJson(bodyRequest).toRequestBody(MEDIA_TYPE_JSON),
            headers = headers,
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

        var submitResponse = submitReview(
                clientId = clientId,
                token = token,
                appId = appId,
                releaseType = 1,
                releaseTime = releaseTime,
                requestBody = "".toRequestBody(MEDIA_TYPE_JSON)
        )
        return getSubmissionCompletedResponse(
                submitResponse = submitResponse,
                clientId = clientId,
                token = token,
                appId = appId,
                releaseTime = releaseTime
        )
    }

    private fun getSubmissionCompletedResponse(
            submitResponse: SubmitResponse,
            clientId: String,
            token: String,
            appId: String,
            releaseTime: String?
    ): SubmitResponse {

        return if (submitResponse.ret.code == 0) {
            submitResponse
        } else if (submitResponse.ret.code == 204144660 && submitResponse.ret.msg.contains("It may take 2-5 minutes")) {
            Logger.i("Build is currently processing, waiting for 3 minutes before submitting again...")
            Thread.sleep(180000)
            var submissionResult = submitReview(
                    clientId = clientId,
                    token = token,
                    appId = appId,
                    releaseType = 1,
                    releaseTime = releaseTime,
                    requestBody = "".toRequestBody(MEDIA_TYPE_JSON)
            )
            submissionResult
        } else
            throw HuaweiHttpResponseException(submitResponse.toString())
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

        return submitReview(
            clientId = clientId,
            token = token,
            appId = appId,
            releaseType = 3,
            releaseTime = null,
            requestBody = gson.toJson(bodyRequest).toRequestBody(MEDIA_TYPE_JSON)
        )
    }

    private fun submitReview(
        clientId: String,
        token: String,
        appId: String,
        releaseType: Int,
        releaseTime: String?,
        requestBody: RequestBody
    ): SubmitResponse {
        val headers = mutableMapOf<String, String>()
        headers["Content-Type"] = "application/json"
        headers["Accept"] = "application/json"
        headers["Authorization"] = "Bearer $token"
        headers["client_id"] = clientId

        val uriBuilder = "$PUBLISH_API_URL/app-submit".toHttpUrl()
            .newBuilder()
            .addQueryParameter("appId", appId)
            .addQueryParameter("releaseType", releaseType.toString())
        if (releaseTime != null) {
            uriBuilder.addQueryParameter("releaseTime", releaseTime)
        }
        val url = uriBuilder.build().toUrl().toString()

        val result = httpClient.post<SubmitResponse>(
            url = url,
            body = requestBody,
            headers = headers,
        )

        return result
    }
}