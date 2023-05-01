package ru.cian.huawei.publish.service

import org.gradle.internal.resource.transport.http.HttpRequestException
import ru.cian.huawei.publish.models.request.FileInfoRequest
import ru.cian.huawei.publish.models.response.AppInfo
import ru.cian.huawei.publish.models.response.FileServerOriResultResponse
import ru.cian.huawei.publish.models.response.Result
import ru.cian.huawei.publish.models.response.Ret
import ru.cian.huawei.publish.models.response.SubmitResponse
import ru.cian.huawei.publish.models.response.UpdateAppFileInfoResponse
import ru.cian.huawei.publish.models.response.UpdateAppBasicInfoResponse
import ru.cian.huawei.publish.models.response.UpdateReleaseNotesResponse
import ru.cian.huawei.publish.models.response.UploadFileRsp
import ru.cian.huawei.publish.models.response.UploadUrlResponse
import java.io.File

private const val REQUEST_RETRIES = 5

@Suppress("StringLiteralDuplication", "TooManyFunctions")
internal class MockHuaweiService : HuaweiService {

    private var retries = 0

    override fun getToken(clientId: String, clientSecret: String) = "MockToken"

    override fun getAppID(
        clientId: String,
        accessToken: String,
        packageName: String
    ) = AppInfo(
        key = "MockKey",
        value = "MockValue"
    )

    override fun getUploadingBuildUrl(
        clientId: String,
        accessToken: String,
        appId: String,
        suffix: String
    ) = UploadUrlResponse(
        ret = Ret(
            code = -1,
            msg = "MockMessage"
        ),
        uploadUrl = "MockUploadUrl",
        chunkUploadUrl = "MockChunkUploadUrl",
        authCode = "MockAuthCode"
    )

    override fun uploadBuildFile(
        uploadUrl: String,
        authCode: String,
        buildFile: File
    ) = FileServerOriResultResponse(
        result = Result(
            resultCode = -1,
            uploadFileRsp = UploadFileRsp(
                ifSuccess = 1,
                fileInfoList = emptyList()
            )
        )
    )

    override fun updateAppFileInformation(
        clientId: String,
        accessToken: String,
        appId: String,
        releaseType: Int,
        fileInfoRequestList: List<FileInfoRequest>
    ) = UpdateAppFileInfoResponse(
        ret = Ret(
            code = -1,
            msg = "MockMessage"
        )
    )

    override fun updateReleaseNotes(
        clientId: String,
        accessToken: String,
        appId: String,
        lang: String,
        newFeatures: String
    ) = UpdateReleaseNotesResponse(
        ret = Ret(
            code = -1,
            msg = "MockMessage"
        )
    )

    override fun submitReviewImmediately(
        clientId: String,
        accessToken: String,
        appId: String,
        releaseTime: String?
    ) = getSubmitResponseWithRetries()

    override fun submitReviewWithReleasePhase(
        clientId: String,
        accessToken: String,
        appId: String,
        startRelease: String?,
        endRelease: String?,
        releasePercent: Double
    ) = getSubmitResponseWithRetries()

    override fun updateAppBasicInfo(
        clientId: String,
        accessToken: String,
        appId: String,
        releaseType: Int,
        appBasicInfo: String
    ) = UpdateAppBasicInfoResponse(
        ret = Ret(
            code = -1,
            msg = "MockMessage"
        )
    )

    @Suppress("ThrowingExceptionsWithoutMessageOrCause")
    private fun getSubmitResponseWithRetries(): SubmitResponse {
        if (retries < REQUEST_RETRIES) {
            retries++
            throw HttpRequestException("That's work as well for MockServer, attempt=$retries", Throwable())
        } else {
            return SubmitResponse(
                ret = Ret(
                    code = -1,
                    msg = "MockMessage"
                )
            )
        }
    }
}
