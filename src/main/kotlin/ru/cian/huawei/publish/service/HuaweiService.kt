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

    fun getUploadApkUrl(
        clientId: String,
        token: String,
        appId: String
    ): UploadUrlResponse

    fun uploadApkFile(
        uploadUrl: String,
        authCode: String,
        apkFile: File
    ): FileServerOriResultResponse

    fun updateAppFileInformation(
        clientId: String,
        token: String,
        appId: String,
        fileInfoRequestList: List<FileInfoRequest>
    ): UpdateAppFileInfoResponse

    fun submitPublication(
        clientId: String,
        token: String,
        appId: String
    ): SubmitResponse
}