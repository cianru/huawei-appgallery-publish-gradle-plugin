package ru.cian.huawei.publish

import com.android.build.api.variant.ApplicationVariant
import org.gradle.api.DefaultTask
import org.gradle.api.publish.plugins.PublishingPlugin
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.work.DisableCachingByDefault
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
import ru.cian.huawei.publish.utils.FileWrapper

@DisableCachingByDefault
open class HuaweiPublishTask
@Inject constructor(
    private val variant: ApplicationVariant
) : DefaultTask() {

    init {
        group = PublishingPlugin.PUBLISH_TASK_GROUP
        description = "Upload and publish application build file " +
            "to Huawei AppGallery Store for ${variant.name} buildType"
    }
    
    private val logger by lazy { Logger(project) }

    @get:Internal
    @set:Option(
        option = "deployType",
        description = "How to deploy build: 'publish' to all users or create 'draft' " +
            "without publishing or 'upload-only' without draft creation"
    )
    var deployType: DeployType? = null

    @get:Internal
    @set:Option(
        option = "publishTimeoutMs",
        description = "The time in millis during which the plugin periodically tries to publish the build"
    )
    var publishTimeoutMs: String? = null

    @get:Internal
    @set:Option(
        option = "publishPeriodMs",
        description = "The period in millis between tries to publish the build"
    )
    var publishPeriodMs: String? = null

    @get:Internal
    @set:Option(
        option = "credentialsPath",
        description = "File path with AppGallery credentials params ('client_id' and 'client_secret')"
    )
    var credentialsPath: String? = null

    @get:Internal
    @set:Option(
        option = "clientId",
        description = "'client_id' param from AppGallery credentials. " +
            "The key more priority than value from 'credentialsPath'"
    )
    var clientId: String? = null

    @get:Internal
    @set:Option(
        option = "clientSecret",
        description = "'client_secret' param from AppGallery credentials. " +
            "The key more priority than value from 'credentialsPath'"
    )
    var clientSecret: String? = null

    @get:Internal
    @set:Option(
        option = "buildFormat",
        description = "'apk' or 'aab' for corresponding build format"
    )
    var buildFormat: BuildFormat? = null

    @get:Internal
    @set:Option(
        option = "buildFile",
        description = "Path to build file. 'null' means use standard path for 'apk' and 'aab' files."
    )
    var buildFile: String? = null

    @get:Internal
    @set:Option(
        option = "releaseTime",
        description = "Release time in UTC format. The format is $RELEASE_DATE_TIME_FORMAT."
    )
    var releaseTime: String? = null

    @get:Internal
    @set:Option(
        option = "releasePhaseStartTime",
        description = "Start release time after review in UTC format. The format is $RELEASE_DATE_TIME_FORMAT."
    )
    var releasePhaseStartTime: String? = null

    @get:Internal
    @set:Option(
        option = "releasePhaseEndTime",
        description = "End release time after review in UTC format. The format is $RELEASE_DATE_TIME_FORMAT."
    )
    var releasePhaseEndTime: String? = null

    @get:Internal
    @set:Option(
        option = "releasePhasePercent",
        description = "Percentage of target users of release by phase. The integer or decimal value from 0 to 100."
    )
    var releasePhasePercent: String? = null

    @SuppressWarnings("MaxLineLength")
    @get:Internal
    @set:Option(
        option = "releaseNotes",
        description = "Release Notes. Format: '<lang_1>:<releaseNotes_FilePath_1>;<lang_2>:<releaseNotes_FilePath_2>'. " +
                "See https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-References/agcapi-reference-langtype-0000001158245079 " +
                "to choose `lang` param"
    )
    var releaseNotes: String? = null

    @get:Internal
    @set:Option(option = "apiStub", description = "Use RestAPI stub instead of real RestAPI requests")
    var apiStub: Boolean? = false

    @Suppress("LongMethod")
    @TaskAction
    fun action() {

        val huaweiService: HuaweiService = if (apiStub == true) MockHuaweiService() else HuaweiServiceImpl(logger)
        val huaweiPublishExtension = project.extensions
            .findByName(HuaweiPublishExtension.MAIN_EXTENSION_NAME) as? HuaweiPublishExtension
            ?: throw IllegalArgumentException(
                "Plugin extension '${HuaweiPublishExtension.MAIN_EXTENSION_NAME}' " +
                    "is not available at build.gradle of the application module"
            )

        val buildTypeName = variant.name
        val extension = huaweiPublishExtension.instances.find { it.name.equals(buildTypeName, ignoreCase = true) }
            ?: throw IllegalArgumentException(
                "Plugin extension '${HuaweiPublishExtension.MAIN_EXTENSION_NAME}' " +
                    "instance with name '$buildTypeName' is not available"
            )

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
            releaseNotes = releaseNotes,
            apiStub = apiStub
        )

        logger.i("extension=$extension")
        logger.i("cli=$cli")

        logger.v("Prepare input config")
        val buildFileProvider = BuildFileProvider(variant = variant, logger = logger)
        val config = ConfigProvider(
            extension = extension,
            cli = cli,
            buildFileProvider = buildFileProvider,
            releaseNotesFileProvider = FileWrapper()
        ).getConfig()
        logger.i("config=$config")

        logger.v("Found build file: `${config.artifactFile.name}`")

        logger.v("Get Access Token")
        val token = huaweiService.getToken(
            clientId = config.credentials.clientId,
            clientSecret = config.credentials.clientSecret
        )
        logger.i("token=$token")

        logger.v("Get App ID")
        val applicationId = variant.applicationId.get()
        val appInfo = huaweiService.getAppID(
            clientId = config.credentials.clientId,
            accessToken = token,
            packageName = applicationId
        )
        logger.i("appInfo=$appInfo")

        logger.v("Get Upload Url")
        val uploadUrl = huaweiService.getUploadingBuildUrl(
            clientId = config.credentials.clientId,
            accessToken = token,
            appId = appInfo.value,
            suffix = config.artifactFormat.fileExtension
        )
        logger.i("uploadUrl=$uploadUrl")

        logger.v("Upload build file '${config.artifactFile.path}'")
        val fileInfoListResult = huaweiService.uploadBuildFile(
            uploadUrl = uploadUrl.uploadUrl,
            authCode = uploadUrl.authCode,
            buildFile = config.artifactFile
        )
        logger.i("fileInfoListResult=$fileInfoListResult")

        if (!config.releaseNotes.isNullOrEmpty()) {
            config.releaseNotes.forEachIndexed { index, releaseNote ->
                logger.v("Upload release notes: ${index + 1}/${config.releaseNotes.size}, lang=${releaseNote.lang}")
                huaweiService.updateReleaseNotes(
                    clientId = config.credentials.clientId,
                    accessToken = token,
                    appId = appInfo.value,
                    lang = releaseNote.lang,
                    newFeatures = releaseNote.newFeatures,
                )
            }
        } else {
            logger.v("Skip release notes uploading")
        }

        if (config.deployType != DeployType.UPLOAD_ONLY) {
            logger.v("Update App File Info")
            val fileInfoRequestList = mapFileInfo(fileInfoListResult, config.artifactFile.name)
            val appId = appInfo.value
            val releasePercent = config.releasePhase?.percent ?: FULL_USER_SUBMISSION_PERCENT
            val releaseType = if (releasePercent == FULL_USER_SUBMISSION_PERCENT) {
                ReleaseType.FULL
            } else {
                ReleaseType.PHASE
            }
            logger.i("fileInfoRequestList=$fileInfoRequestList")
            val updateAppFileInformation = huaweiService.updateAppFileInformation(
                clientId = config.credentials.clientId,
                accessToken = token,
                appId = appId,
                releaseType = releaseType.type,
                fileInfoRequestList = fileInfoRequestList
            )
            logger.i("updateAppFileInformation=$updateAppFileInformation")

            if (config.deployType == DeployType.PUBLISH) {
                logger.v("Submit Review")

                val submitRequestFunction: () -> SubmitResponse = {
                    getSubmitResponse(
                        releaseType = releaseType,
                        huaweiService = huaweiService,
                        config = config,
                        token = token,
                        appId = appId,
                        releasePercent = releasePercent,
                    )
                }

                submitReleaseByServerPolling(
                    publishPeriodMs = config.publishPeriodMs,
                    publishTimeoutMs = config.publishTimeoutMs,
                    action = {
                        val submitResponse = submitRequestFunction.invoke()
                        logger.i("submitResponse=$submitResponse")
                        submitResponse.ret
                    }
                )

                logger.v("Upload build file with submit on $releasePercent% users - Successfully Done!")
            } else {
                logger.v("Upload build file draft without submit on users - Successfully Done!")
            }
        } else {
            logger.v("Upload build file without draft and submit on users - Successfully Done!")
        }
    }

    @SuppressWarnings("LongParameterList")
    private fun getSubmitResponse(
        releaseType: ReleaseType,
        huaweiService: HuaweiService,
        config: HuaweiPublishConfig,
        token: String,
        appId: String,
        releasePercent: Double
    ): SubmitResponse {
        return when (releaseType) {
            ReleaseType.FULL -> {
                huaweiService.submitReviewImmediately(
                    clientId = config.credentials.clientId,
                    accessToken = token,
                    appId = appId,
                    releaseTime = config.releaseTime
                )
            }
            ReleaseType.PHASE -> {
                huaweiService.submitReviewWithReleasePhase(
                    clientId = config.credentials.clientId,
                    accessToken = token,
                    appId = appId,
                    startRelease = config.releasePhase?.startTime,
                    endRelease = config.releasePhase?.endTime,
                    releasePercent = releasePercent
                )
            }
        }
    }

    private fun submitReleaseByServerPolling(
        publishPeriodMs: Long,
        publishTimeoutMs: Long,
        action: (() -> Unit)
    ) {
        ServerPollingExecutor().run(
            periodTimeInMs = publishPeriodMs,
            timeoutInMs = publishTimeoutMs,
            action = {
                action.invoke()
            },
            processListener = { timeLeft, exception ->
                logger.v("Action failed! Reason: '$exception'. " +
                    "Timeout left '${timeLeft.toHumanPrettyFormatInterval()}'.")
            },
            successListener = {
                logger.v("Uploading successfully finished")
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
        FULL(type = 1),
        PHASE(type = 3)
    }

    companion object {
        const val TASK_NAME = "publishHuaweiAppGallery"
        private const val FULL_USER_SUBMISSION_PERCENT = 100.0
    }
}
