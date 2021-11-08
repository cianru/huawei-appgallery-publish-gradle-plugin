package ru.cian.huawei.publish.service

import ru.cian.huawei.publish.models.request.FileInfoRequest
import ru.cian.huawei.publish.models.response.AppInfo
import ru.cian.huawei.publish.models.response.FileServerOriResultResponse
import ru.cian.huawei.publish.models.response.SubmitResponse
import ru.cian.huawei.publish.models.response.UpdateAppFileInfoResponse
import ru.cian.huawei.publish.models.response.UploadUrlResponse
import java.io.File

/**
 * Huawei Publish API v2 on each request return response code with code 200;
 */
internal interface HuaweiService {

    fun getToken(
        clientId: String,
        clientSecret: String
    ): String

    fun getAppID(
        clientId: String,
        token: String,
        packageName: String
    ): AppInfo

    fun getUploadingBuildUrl(
        clientId: String,
        token: String,
        appId: String,
        suffix: String
    ): UploadUrlResponse

    fun uploadBuildFile(
        uploadUrl: String,
        authCode: String,
        buildFile: File
    ): FileServerOriResultResponse

    fun updateAppFileInformation(
        clientId: String,
        token: String,
        appId: String,
        releaseType: Int,
        fileInfoRequestList: List<FileInfoRequest>
    ): UpdateAppFileInfoResponse

    /**
     * Submit build on 100% users immediately;
     */
    fun submitReviewImmediately(
        clientId: String,
        token: String,
        appId: String,
        releaseTime: String?
    ): SubmitResponse

    /**
     * Submit build with release phase;
     */
    fun submitReviewWithReleasePhase(
        clientId: String,
        token: String,
        appId: String,
        startRelease: String? = null,
        endRelease: String? = null,
        releasePercent: Double
    ): SubmitResponse
}
