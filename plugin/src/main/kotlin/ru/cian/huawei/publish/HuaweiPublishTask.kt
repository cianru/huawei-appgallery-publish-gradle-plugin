package ru.cian.huawei.publish

import com.android.build.api.variant.ApplicationVariant
import org.gradle.api.DefaultTask
import org.gradle.api.publish.plugins.PublishingPlugin
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import ru.cian.huawei.publish.models.request.FileInfoRequest
import ru.cian.huawei.publish.models.response.FileServerOriResultResponse
import ru.cian.huawei.publish.models.response.SubmitResponse
import ru.cian.huawei.publish.service.HuaweiService
import ru.cian.huawei.publish.service.HuaweiServiceImpl
import ru.cian.huawei.publish.service.MockHuaweiService
import ru.cian.huawei.publish.utils.BuildFileProvider
import ru.cian.huawei.publish.utils.ConfigProvider
import ru.cian.huawei.publish.utils.Logger
import ru.cian.huawei.publish.utils.RELEASE_DATE_TIME_FORMAT
import ru.cian.huawei.publish.utils.ServerPollingExecutor
import ru.cian.huawei.publish.utils.toHumanPrettyFormatInterval
import javax.inject.Inject

open class HuaweiPublishTask
@Inject constructor(
    private val variant: ApplicationVariant
) : DefaultTask() {

    init {
        group = PublishingPlugin.PUBLISH_TASK_GROUP
        description = "Upload and publish application build file to Huawei AppGallery Store for ${variant.name} buildType"
    }

    @get:Internal
    @set:Option(option = "deployType", description = "How to deploy build: 'publish' to all users or create 'draft' without publishing or 'upload-only' without draft creation")
    var deployType: DeployType? = null

    @get:Internal
    @set:Option(option = "publishTimeoutMs", description = "The time in millis during which the plugin periodically tries to publish the build")
    var publishTimeoutMs: String? = null

    @get:Internal
    @set:Option(option = "publishPeriodMs", description = "The period in millis between tries to publish the build")
    var publishPeriodMs: String? = null

    @get:Internal
    @set:Option(option = "credentialsPath", description = "File path with AppGallery credentials params ('client_id' and 'client_secret')")
    var credentialsPath: String? = null

    @get:Internal
    @set:Option(option = "clientId", description = "'client_id' param from AppGallery credentials. The key more priority than value from 'credentialsPath'")
    var clientId: String? = null

    @get:Internal
    @set:Option(option = "clientSecret", description = "'client_secret' param from AppGallery credentials. The key more priority than value from 'credentialsPath'")
    var clientSecret: String? = null

    @get:Internal
    @set:Option(option = "buildFormat", description = "'apk' or 'aab' for corresponding build format")
    var buildFormat: BuildFormat? = null

    @get:Internal
    @set:Option(option = "buildFile", description = "Path to build file. 'null' means use standard path for 'apk' and 'aab' files.")
    var buildFile: String? = null

    @get:Internal
    @set:Option(option = "releaseTime", description = "Release time in UTC format. The format is $RELEASE_DATE_TIME_FORMAT.")
    var releaseTime: String? = null

    @get:Internal
    @set:Option(option = "releasePhaseStartTime", description = "Start release time after review in UTC format. The format is $RELEASE_DATE_TIME_FORMAT.")
    var releasePhaseStartTime: String? = null

    @get:Internal
    @set:Option(option = "releasePhaseEndTime", description = "End release time after review in UTC format. The format is $RELEASE_DATE_TIME_FORMAT.")
    var releasePhaseEndTime: String? = null

    @get:Internal
    @set:Option(option = "releasePhasePercent", description = "Percentage of target users of release by phase. The integer or decimal value from 0 to 100.")
    var releasePhasePercent: String? = null

    @get:Internal
    @set:Option(option = "apiStub", description = "Use RestAPI stub instead of real RestAPI requests")
    var apiStub: Boolean? = false

    @TaskAction
    fun action() {

        val huaweiService: HuaweiService = if (apiStub == true) MockHuaweiService() else HuaweiServiceImpl()
        val huaweiPublishExtension = project.extensions.findByName(HuaweiPublishExtension.MAIN_EXTENSION_NAME) as? HuaweiPublishExtension
            ?: throw IllegalArgumentException("Plugin extension '${HuaweiPublishExtension.MAIN_EXTENSION_NAME}' is not available at build.gradle of the application module")

        val buildTypeName = variant.name
        val extension = huaweiPublishExtension.instances.find { it.name.equals(buildTypeName, ignoreCase = true) }
            ?: throw IllegalArgumentException("Plugin extension '${HuaweiPublishExtension.MAIN_EXTENSION_NAME}' instance with name '$buildTypeName' is not available")

        val cli = HuaweiPublishCliParam(
            deployType = deployType,
            publishTimeoutMs = publishTimeoutMs,
            publishPeriodMs = publishPeriodMs,
            credentialsPath = credentialsPath,
            clientId = clientId,
            clientSecret = clientSecret,
            buildFormat = buildFormat,
            buildFile = buildFile,
            releaseTime = releaseTime,
            releasePhaseStartTime = releasePhaseStartTime,
            releasePhaseEndTime = releasePhaseEndTime,
            releasePhasePercent = releasePhasePercent,
            apiStub = apiStub
        )

        Logger.i("Generate Config")
        val buildFileProvider = BuildFileProvider(variant)
        val config = ConfigProvider(
            extension = extension,
            cli = cli,
            buildFileProvider = buildFileProvider
        ).getConfig()
        Logger.i("Found build file: `${config.artifactFile.name}`")

        Logger.i("Get Access Token")
        val token = huaweiService.getToken(
            clientId = config.credentials.clientId,
            clientSecret = config.credentials.clientSecret
        )

        Logger.i("Get App ID")
        val applicationId = variant.applicationId.get()
        val appInfo = huaweiService.getAppID(
            clientId = config.credentials.clientId,
            token = token,
            packageName = applicationId
        )

        Logger.i("Get Upload Url")
        val uploadUrl = huaweiService.getUploadingBuildUrl(
            clientId = config.credentials.clientId,
            token = token,
            appId = appInfo.value,
            suffix = config.artifactFormat.fileExtension
        )

        Logger.i("Upload build file '${config.artifactFile.path}'")
        val fileInfoListResult = huaweiService.uploadBuildFile(
            uploadUrl = uploadUrl.uploadUrl,
            authCode = uploadUrl.authCode,
            buildFile = config.artifactFile
        )

        if (config.deployType != DeployType.UPLOAD_ONLY) {
            Logger.i("Update App File Info")
            val fileInfoRequestList = mapFileInfo(fileInfoListResult, config.artifactFile.name)
            val appId = appInfo.value
            val releasePercent = config.releasePhase?.percent ?: 100.0
            val releaseType = if (releasePercent == 100.0) {
                ReleaseType.FULL
            } else {
                ReleaseType.PHASE
            }
            huaweiService.updateAppFileInformation(
                clientId = config.credentials.clientId,
                token = token,
                appId = appId,
                releaseType = releaseType.type,
                fileInfoRequestList = fileInfoRequestList
            )

            if (config.deployType == DeployType.PUBLISH) {
                Logger.i("Submit Review")

                val submitResponse: () -> SubmitResponse = {
                    when (releaseType) {
                        ReleaseType.FULL -> {
                            huaweiService.submitReviewImmediately(
                                clientId = config.credentials.clientId,
                                token = token,
                                appId = appId,
                                releaseTime = config.releaseTime
                            )
                        }
                        ReleaseType.PHASE -> {
                            huaweiService.submitReviewWithReleasePhase(
                                clientId = config.credentials.clientId,
                                token = token,
                                appId = appId,
                                startRelease = config.releasePhase?.startTime,
                                endRelease = config.releasePhase?.endTime,
                                releasePercent = releasePercent
                            )
                        }
                    }
                }

                when (config.artifactFormat) {
                    BuildFormat.APK -> {
                        submitResponse.invoke().ret
                    }
                    BuildFormat.AAB -> {
                        submitReleaseByServerPolling(
                            publishPeriodMs = config.publishPeriodMs,
                            publishTimeoutMs = config.publishTimeoutMs,
                            releasePercent = releasePercent,
                            action = {
                                submitResponse.invoke().ret
                            }
                        )
                    }
                }

                Logger.i("Upload build file with submit on $releasePercent% users - Successfully Done!")
            } else {
                Logger.i("Upload build file draft without submit on users - Successfully Done!")
            }
        } else {
            Logger.i("Upload build file without draft and submit on users - Successfully Done!")
        }
    }

    private fun submitReleaseByServerPolling(
        publishPeriodMs: Long,
        publishTimeoutMs: Long,
        releasePercent: Double,
        action: (() -> Unit)
    ) {
        ServerPollingExecutor().run(
            periodTimeInMs = publishPeriodMs,
            timeoutInMs = publishTimeoutMs,
            action = {
                action.invoke()
            },
            processListener = { timeLeft, exception ->
                Logger.i("Action failed! Reason: '$exception'. Timeout left '${timeLeft.toHumanPrettyFormatInterval()}'.")
            },
            successListener = {
                Logger.i("Uploading successfully finished")
            },
            failListener = { lastException ->
                throw lastException ?: RuntimeException("Unknown error")
            }
        )
    }

    private fun mapFileInfo(
        fileInfoListResult: FileServerOriResultResponse,
        buildFileName: String
    ): MutableList<FileInfoRequest> {
        val fileInfoList = fileInfoListResult.result.uploadFileRsp?.fileInfoList
        val fileInfoRequestList = mutableListOf<FileInfoRequest>()
        fileInfoList?.forEach {
            fileInfoRequestList.add(
                FileInfoRequest(
                    fileName = buildFileName,
                    fileDestUrl = it.fileDestUlr,
                    size = it.size
                )
            )
        }
        return fileInfoRequestList
    }

    internal enum class ReleaseType(val type: Int) {
        FULL(1),
        PHASE(3)
    }

    companion object {
        const val NAME = "publishHuaweiAppGallery"
    }
}

