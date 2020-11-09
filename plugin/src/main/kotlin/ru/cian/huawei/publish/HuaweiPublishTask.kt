package ru.cian.huawei.publish

import com.android.build.gradle.api.BaseVariant
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import ru.cian.huawei.publish.models.BuildFormat
import ru.cian.huawei.publish.models.HuaweiPublishCliParam
import ru.cian.huawei.publish.models.HuaweiPublishExtension
import ru.cian.huawei.publish.models.request.FileInfoRequest
import ru.cian.huawei.publish.models.response.FileServerOriResultResponse
import ru.cian.huawei.publish.models.response.SubmitResponse
import ru.cian.huawei.publish.service.HuaweiService
import ru.cian.huawei.publish.service.HuaweiServiceImpl
import ru.cian.huawei.publish.utils.*
import ru.cian.huawei.publish.utils.ConfigProvider
import ru.cian.huawei.publish.utils.Logger
import ru.cian.huawei.publish.utils.ServerPollingExecutor
import ru.cian.huawei.publish.utils.toHumanPrettyFormatInterval
import javax.inject.Inject

open class HuaweiPublishTask
@Inject constructor(
    private val variant: BaseVariant
) : DefaultTask() {

    init {
        group = "Huawei App Gallery Publishing"
        description = "Upload and publish application build file to Huawei AppGallery Store for ${variant.baseName} buildType"
    }

    @get:Internal
    @set:Option(option = "no-publish", description = "To disable publishing the build file on all users after uploading")
    var noPublish: Boolean? = null
        set(value) {
            if (value != null) {
                field = !value
            }
        }

    @get:Internal
    @set:Option(option = "publish", description = "To enable publishing the build file on all users after uploading")
    var publish: Boolean? = null

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

        val huaweiService: HuaweiService = HuaweiServiceImpl()
        val huaweiPublishExtension = project.extensions.findByName(HuaweiPublishExtension.MAIN_EXTENSION_NAME) as? HuaweiPublishExtension
            ?: throw IllegalArgumentException("Plugin extension '${HuaweiPublishExtension.MAIN_EXTENSION_NAME}' is not available at build.gradle of the application module")

        val buildTypeName = variant.name
        val extension = huaweiPublishExtension.instances.find { it.name.toLowerCase() == buildTypeName. toLowerCase() }
            ?: throw IllegalArgumentException("Plugin extension '${HuaweiPublishExtension.MAIN_EXTENSION_NAME}' instance with name '$buildTypeName' is not available")

        val cli = HuaweiPublishCliParam(
            noPublish = noPublish,
            publish = publish,
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
        val appInfo = huaweiService.getAppID(
            clientId = config.credentials.clientId,
            token = token,
            packageName = variant.applicationId
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

        if (config.publish) {
            Logger.i("Submit Review")

            val submitActionFunction: Lazy<SubmitResponse> = lazy {
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

            when (buildFormat) {
                BuildFormat.APK -> {
                    submitActionFunction.value
                }
                BuildFormat.AAB -> {
                    submitReleaseByServerPolling(
                        publishPeriodMs = config.publishPeriodMs,
                        publishTimeoutMs = config.publishTimeoutMs,
                        releasePercent = releasePercent,
                        action = {
                            submitActionFunction.value
                        }
                    )
                }
            }

            Logger.i("Upload build file with submit on $releasePercent% users - Successfully Done!")
        } else {
            Logger.i("Upload build file without submit on users - Successfully Done!")
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
                Logger.i("Upload build file with submit on $releasePercent% users - Successfully Done!")
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
