package ru.cian.huawei.publish.service

import ru.cian.huawei.publish.models.request.FileInfoRequest
import ru.cian.huawei.publish.models.response.AppInfo
import ru.cian.huawei.publish.models.response.FileServerOriResultResponse
import ru.cian.huawei.publish.models.response.SubmitResponse
import ru.cian.huawei.publish.models.response.UpdateAppFileInfoResponse
import ru.cian.huawei.publish.models.response.UploadUrlResponse
import java.io.File
import ru.cian.huawei.publish.models.response.UpdateReleaseNotesResponse

/**
 * Huawei Publish API v2 on each request return response code with code 200;
 */
internal interface HuaweiService {

    /**
     * See documentation
     * https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-References/agcapi-obtain_token-0000001158365043
     */
    fun getToken(
        clientId: String,
        clientSecret: String
    ): String

    /**
     * See documentation
     * https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-References/agcapi-appid-list-0000001111845086
     */
    fun getAppID(
        clientId: String,
        accessToken: String,
        packageName: String
    ): AppInfo

    /**
     * See documentation
     * https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-References/agcapi-upload-url-0000001158365047
     */
    fun getUploadingBuildUrl(
        clientId: String,
        accessToken: String,
        appId: String,
        suffix: String
    ): UploadUrlResponse

    /**
     * See documentation
     * https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-References/agcapi-upload-file-0000001158245059
     */
    fun uploadBuildFile(
        uploadUrl: String,
        authCode: String,
        buildFile: File
    ): FileServerOriResultResponse

    /**
     * See documentation
     * https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-References/agcapi-app-file-info-0000001111685202
     */
    fun updateAppFileInformation(
        clientId: String,
        accessToken: String,
        appId: String,
        releaseType: Int,
        fileInfoRequestList: List<FileInfoRequest>
    ): UpdateAppFileInfoResponse

    /**
     * Submit build on 100% users immediately
     * https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-References/agcapi-app-submit-0000001158245061
     */
    fun submitReviewImmediately(
        clientId: String,
        accessToken: String,
        appId: String,
        releaseTime: String?
    ): SubmitResponse

    /**
     * See documentation
     * https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-References/agcapi-language-info-update-0000001158245057
     */
    fun updateReleaseNotes(
        clientId: String,
        accessToken: String,
        appId: String,
        lang: String,
        newFeatures: String,
    ): UpdateReleaseNotesResponse

    /**
     * Submit build with release phase
     * https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-References/agcapi-app-submit-0000001158245061
     */
    fun submitReviewWithReleasePhase(
        clientId: String,
        accessToken: String,
        appId: String,
        startRelease: String? = null,
        endRelease: String? = null,
        releasePercent: Double
    ): SubmitResponse
}
