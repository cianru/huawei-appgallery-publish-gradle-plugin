package ru.cian.huawei.publish

import com.android.build.api.artifact.ArtifactType
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.api.InstallableVariantImpl
import com.android.build.gradle.internal.scope.InternalArtifactType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.file.RegularFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import ru.cian.huawei.publish.models.Credential
import ru.cian.huawei.publish.models.request.FileInfoRequest
import ru.cian.huawei.publish.models.response.FileServerOriResultResponse
import ru.cian.huawei.publish.models.response.SubmitResponse
import ru.cian.huawei.publish.service.HuaweiService
import ru.cian.huawei.publish.service.HuaweiServiceImpl
import ru.cian.huawei.publish.utils.Logger
import ru.cian.huawei.publish.utils.nullIfBlank
import ru.cian.huawei.publish.utils.toHumanPrettyFormatInterval
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

private const val DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZZ"
private const val DEFAULT_PUBLISH_TIMEOUT_MS = 10 * 60 * 1000L
private const val DEFAULT_PUBLISH_PERIOD_MS = 15 * 1000L

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
    @set:Option(option = "releaseTime", description = "Release time in UTC format. The format is $DATETIME_FORMAT.")
    var releaseTime: String? = null

    @get:Internal
    @set:Option(option = "releasePhaseStartTime", description = "Start release time after review in UTC format. The format is $DATETIME_FORMAT.")
    var releasePhaseStartTime: String? = null

    @get:Internal
    @set:Option(option = "releasePhaseEndTime", description = "End release time after review in UTC format. The format is $DATETIME_FORMAT.")
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
            ?: throw IllegalArgumentException("Plugin extension '${HuaweiPublishExtension.MAIN_EXTENSION_NAME}' is not available at app/build.gradle")

        val buildTypeName = variant.name
        val extension = huaweiPublishExtension.instances.find { it.name.toLowerCase() == buildTypeName. toLowerCase() }
            ?: throw IllegalArgumentException("Plugin extension '${HuaweiPublishExtension.MAIN_EXTENSION_NAME}' instance with name '$buildTypeName' is not available")

        val publish = this.noPublish ?: this.publish ?: extension.publish ?: true
        val publishTimeoutMs = this.publishTimeoutMs?.toLong() ?: extension.publishTimeoutMs ?: DEFAULT_PUBLISH_TIMEOUT_MS
        val publishPeriodMs = this.publishPeriodMs?.toLong() ?: extension.publishPeriodMs ?: DEFAULT_PUBLISH_PERIOD_MS
        val credentialsFilePath = this.credentialsPath ?: extension.credentialsPath
        val clientIdPriority: String? = this.clientId ?: extension.clientId
        val clientSecretPriority: String? = this.clientSecret ?: extension.clientSecret
        val buildFormat = this.buildFormat ?: extension.buildFormat
        val buildFile: String? = this.buildFile ?: extension.buildFile
        val releaseTime: String? = this.releaseTime ?: extension.releaseTime
        val releasePhase = ReleasePhaseExtension()
        releasePhase.startTime = this.releasePhaseStartTime ?: extension.releasePhase?.startTime
        releasePhase.endTime = this.releasePhaseEndTime ?: extension.releasePhase?.endTime
        releasePhase.percent = this.releasePhasePercent?.toDouble() ?: extension.releasePhase?.percent

        if (releasePhase != null) {
            if (releasePhase.percent == null || releasePhase.percent!! <= 0 && releasePhase.percent!! > 100) {
                throw IllegalArgumentException("Wrong percent release phase value = '$releasePhase.percent'. Allowed values between 0 and 100 with up to two decimal places.")
            }

            val nowCalendar = Calendar.getInstance()
            val sdf = SimpleDateFormat(DATETIME_FORMAT, Locale.getDefault())

            val endCalendar = Calendar.getInstance()
            endCalendar.time = sdf.parse(releasePhase.endTime)
            if (endCalendar.before(nowCalendar)) {
                throw IllegalArgumentException("Wrong endTime release phase value = '${releasePhase.endTime}'. It less than current moment.")
            }

            val startCalendar = Calendar.getInstance()
            startCalendar.time = sdf.parse(releasePhase.startTime)
            if (startCalendar.after(endCalendar)) {
                throw IllegalArgumentException("Wrong startTime release phase value = '${releasePhase.startTime}'. It bigger than endTime = '${releasePhase.endTime}'.")
            }
        }

        Logger.i("Get Credentials")
        val credentials = lazy {
            val credentialsFile = File(credentialsFilePath)
            if (!credentialsFile.exists()) {
                throw FileNotFoundException("$huaweiPublishExtension (File (${credentialsFile.absolutePath}) with 'client_id' and 'client_secret' for access to Huawei Publish API is not found)")
            }
            getCredentials(credentialsFile)
        }
        val clientId = clientIdPriority ?: credentials.value.clientId.nullIfBlank()
            ?: throw IllegalArgumentException("(Huawei credential `clientId` param is null or empty). Please check your credentials file content or as single parameter.")
        val clientSecret = clientSecretPriority ?: credentials.value.clientSecret.nullIfBlank()
            ?: throw IllegalArgumentException("(Huawei credential `clientSecret` param is null or empty). Please check your credentials file content or as single parameter.")

        val apkBuildFiles = when {
            buildFile != null -> File(buildFile)
            buildFormat == BuildFormat.APK -> getFinalApkArtifactCompat(variant)
            buildFormat == BuildFormat.AAB -> getFinalBundleArtifactCompat(variant).singleOrNull()
            else -> throw FileNotFoundException("Could not detect build file path")
        }

        if (apkBuildFiles == null || !apkBuildFiles.exists()) {
            throw FileNotFoundException("$apkBuildFiles (No such file or directory). Please run `assemble*` or `bundle*` task to build the application file before current task.")
        }

        if (buildFormat.fileExtension != apkBuildFiles.extension) {
            throw IllegalArgumentException("Build file ${apkBuildFiles.absolutePath} has wrong file extension that doesn't match with announced buildFormat($buildFormat) plugin extension param.")
        }

        Logger.i("-----------------------------------------")
        Logger.i("releasePhase=$releasePhase")
        Logger.i("-----------------------------------------")

        return

        val buildFileName = apkBuildFiles.name
        Logger.i("Found build file: `${buildFileName}`")

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
        val uploadUrl = huaweiService.getUploadingBuildUrl(
            clientId = clientId,
            token = token,
            appId = appInfo.value,
            suffix = buildFormat.fileExtension
        )

        Logger.i("Upload build file '${apkBuildFiles.path}'")
        val fileInfoListResult = huaweiService.uploadBuildFile(
            uploadUrl = uploadUrl.uploadUrl,
            authCode = uploadUrl.authCode,
            buildFile = apkBuildFiles
        )

        Logger.i("Update App File Info")
        val fileInfoRequestList = mapFileInfo(fileInfoListResult, buildFileName)
        val appId = appInfo.value
        val releasePercent = releasePhase?.percent ?: 100.0
        val releaseType = if (releasePercent == 100.0) {
            ReleaseType.FULL
        } else {
            ReleaseType.PHASE
        }
        huaweiService.updateAppFileInformation(
            clientId = clientId,
            token = token,
            appId = appId,
            releaseType = releaseType.type,
            fileInfoRequestList = fileInfoRequestList
        )

        if (publish) {
            Logger.i("Submit Review")

            val submitActionFunction: Lazy<SubmitResponse> = lazy {
                when (releaseType) {
                    ReleaseType.FULL -> {
                        huaweiService.submitReviewImmediately(
                            clientId = clientId,
                            token = token,
                            appId = appId,
                            releaseTime = releaseTime
                        )
                    }
                    ReleaseType.PHASE -> {
                        huaweiService.submitReviewWithReleasePhase(
                            clientId = clientId,
                            token = token,
                            appId = appId,
                            startRelease = releasePhase?.startTime,
                            endRelease = releasePhase?.endTime,
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
                        publishPeriodMs = publishPeriodMs,
                        publishTimeoutMs = publishTimeoutMs,
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
        ActionExecutor().run(
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

    private fun getCredentials(credentialsFile: File): Credential {
        val reader = JsonReader(FileReader(credentialsFile.absolutePath))
        val type = object : TypeToken<Credential>() {}.type
        return Gson().fromJson(reader, type)
    }

    private fun getFinalApkArtifactCompat(variant: BaseVariant): File {
        return variant.outputs.first().outputFile
    }

    @Suppress("UNCHECKED_CAST") // We know its type
    private fun getFinalBundleArtifactCompat(variant: BaseVariant): Set<File> {
        val installable = variant as InstallableVariantImpl
        return try {
            installable.getFinalArtifact(
                InternalArtifactType.BUNDLE as ArtifactType<FileSystemLocation>
            ).get().files
        } catch (e: NoClassDefFoundError) {
            val enumMethod =
                InternalArtifactType::class.java.getMethod("valueOf", String::class.java)
            val artifactType = enumMethod.invoke(null, "BUNDLE") as ArtifactType<RegularFile>
            val artifact = installable.javaClass
                .getMethod("getFinalArtifact", ArtifactType::class.java)
                .invoke(installable, artifactType)
            artifact.javaClass.getMethod("getFiles").apply {
                isAccessible = true
            }.invoke(artifact) as Set<File>
        }
    }

    enum class ReleaseType(val type: Int) {
        FULL(1),
        PHASE(3)
    }

    companion object {
        const val NAME = "publishHuaweiAppGallery"
    }
}