package ru.cian.huawei.publish

import com.android.build.gradle.api.BaseVariant
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import ru.cian.huawei.publish.models.Credential
import ru.cian.huawei.publish.models.request.FileInfoRequest
import ru.cian.huawei.publish.models.response.FileServerOriResultResponse
import ru.cian.huawei.publish.utils.Logger
import ru.cian.huawei.publish.service.HuaweiService
import ru.cian.huawei.publish.service.HuaweiServiceImpl
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import javax.inject.Inject

open class HuaweiPublishTask
@Inject constructor(
    private val variant: BaseVariant
) : DefaultTask() {

    init {
        group = "Huawei App Gallery Publishing"
        description =
            "Upload and publish APK file to Huawei AppGallery Store for ${variant.baseName} buildType"
    }

    @TaskAction
    fun action() {

        val huaweiService: HuaweiService = HuaweiServiceImpl()
        val huaweiPublishExtension =
            project.extensions.findByName(HuaweiPublishExtension.NAME) as HuaweiPublishExtension

        val credentialsFilePath = huaweiPublishExtension.credentialsPath
        val credentialsFile = File(credentialsFilePath)
        if (!credentialsFile.exists()) {
            throw FileNotFoundException("$credentialsFilePath (File with client_id and client_key for access to Huawei Publish API is not found)")
        }

        val apkFile = variant.outputs.first().outputFile
        if (!apkFile.exists()) {
            throw FileNotFoundException("$apkFile (No such file or directory). Please run `assembleRelease` task before.")
        }

        val apkFileName = apkFile.name

        Logger.i("Get Credentials")
        val credentials = getCredentials(credentialsFilePath)
        val clientId = credentials.clientId
        val clientSecret = credentials.clientKey

        Logger.i("Get Access Token")
        val token = huaweiService.getToken(
            clientId = clientId,
            clientSecret = clientSecret
        )

        Logger.i("Get App ID")
        val appInfo = huaweiService.getAppID(
            clientId = clientId,
            token = token,
            packageName = variant.applicationId
        )

        Logger.i("Get Upload Url")
        val uploadUrl = huaweiService.getUploadApkUrl(
            clientId = clientId,
            token = token,
            appId = appInfo.value
        )

        Logger.i("Upload APK '${apkFile.path}'")
        val fileInfoListResult = huaweiService.uploadApkFile(
            uploadUrl = uploadUrl.uploadUrl,
            authCode = uploadUrl.authCode,
            apkFile = apkFile
        )

        Logger.i("Update App File Info")
        val fileInfoRequestList = mapFileInfo(fileInfoListResult, apkFileName)
        val appId = appInfo.value
        huaweiService.updateAppFileInformation(
            clientId = clientId,
            token = token,
            appId = appId,
            fileInfoRequestList = fileInfoRequestList
        )

        Logger.i("Submit Release")
        huaweiService.submitPublication(
            clientId = clientId,
            token = token,
            appId = appId
        )

        Logger.i("Successfully Done!")
    }

    private fun mapFileInfo(
        fileInfoListResult: FileServerOriResultResponse,
        apkFileName: String
    ): MutableList<FileInfoRequest> {
        val fileInfoList = fileInfoListResult.result.uploadFileRsp?.fileInfoList
        val fileInfoRequestList = mutableListOf<FileInfoRequest>()
        fileInfoList?.forEach {
            fileInfoRequestList.add(
                FileInfoRequest(
                    fileName = apkFileName,
                    fileDestUrl = it.fileDestUlr,
                    size = it.size
                )
            )
        }
        return fileInfoRequestList
    }

    private fun getCredentials(credentialsFilePath: String): Credential {
        val reader = JsonReader(FileReader(credentialsFilePath))
        val type = object : TypeToken<Credential>() {}.type
        return Gson().fromJson(reader, type)
    }

    companion object {
        const val NAME = "publishHuaweiAppGallery"
    }
}