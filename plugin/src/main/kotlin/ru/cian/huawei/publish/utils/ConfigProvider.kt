package ru.cian.huawei.publish.utils

import java.io.File
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import ru.cian.huawei.publish.BuildFormat
import ru.cian.huawei.publish.Credentials
import ru.cian.huawei.publish.HuaweiPublishCliParam
import ru.cian.huawei.publish.HuaweiPublishConfig
import ru.cian.huawei.publish.HuaweiPublishExtensionConfig
import ru.cian.huawei.publish.ReleaseNotesConfig
import ru.cian.huawei.publish.ReleaseNotesDescriptionsConfig
import ru.cian.huawei.publish.ReleasePhaseConfig

internal class ConfigProvider(
    private val extension: HuaweiPublishExtensionConfig,
    private val cli: HuaweiPublishCliParam,
    private val buildFileProvider: BuildFileProvider,
    private val releaseNotesFileProvider: FileWrapper,
) {

    fun getConfig(): HuaweiPublishConfig {

        val deployType = cli.deployType ?: extension.deployType
        val publishTimeoutMs = cli.publishTimeoutMs?.toLong() ?: extension.publishTimeoutMs
        val publishPeriodMs = cli.publishPeriodMs?.toLong() ?: extension.publishPeriodMs
        val artifactFormat = cli.buildFormat ?: extension.buildFormat
        val customBuildFilePath: String? = cli.buildFile ?: extension.buildFile
        val releaseTime: String? = cli.releaseTime ?: extension.releaseTime
        val releasePhase = getReleasePhaseConfig()
        val credentialsConfig = getCredentialsConfig()
        val releaseNotes = getReleaseNotesConfig()
        val appBasicInfoFile = getAppBasicInfoFile()

        val artifactFile = getBuildFile(customBuildFilePath, artifactFormat)

        val artifactFileExtension = artifactFile.extension
        val actualArtifactFormat = when (artifactFileExtension) {
            "apk" -> BuildFormat.APK
            "aab" -> BuildFormat.AAB
            else -> throw IllegalArgumentException(
                "Not allowed artifact file extension: `$artifactFileExtension`. " +
                    "It should be `apk` or `aab`. "
            )
        }

        return HuaweiPublishConfig(
            credentials = credentialsConfig,
            deployType = deployType,
            artifactFormat = actualArtifactFormat,
            artifactFile = artifactFile,
            publishTimeoutMs = publishTimeoutMs,
            publishPeriodMs = publishPeriodMs,
            releaseTime = releaseTime,
            releasePhase = releasePhase,
            releaseNotes = releaseNotes,
            appBasicInfoFile = appBasicInfoFile
        )
    }

    fun getBuildFile(
        customBuildFilePath: String?,
        artifactFormat: BuildFormat
    ): File {

        val artifactFile = if (customBuildFilePath != null) {
            File(customBuildFilePath)
        } else {
            buildFileProvider.getBuildFile(artifactFormat)
        }

        if (artifactFile == null || !artifactFile.exists()) {
            throw FileNotFoundException(
                "$artifactFile (No such file or directory). Application build file is not found. " +
                        "Please run `assemble` or `bundle` task to build the application file before current task."
            )
        }

        if (artifactFormat.fileExtension != artifactFile.extension) {
            throw IllegalArgumentException(
                "Build file ${artifactFile.absolutePath} has wrong file extension " +
                        "that doesn't match with announced buildFormat($artifactFormat) plugin extension param."
            )
        }
        return artifactFile
    }

    @Suppress("ThrowsCount")
    fun getCredentialsConfig(): Credentials {
        val credentialsFilePath = cli.credentialsPath ?: extension.credentialsPath
        val clientIdPriority: String? = cli.clientId
        val clientSecretPriority: String? = cli.clientSecret
        val credentials = lazy {
            if (credentialsFilePath.isNullOrBlank()) {
                throw FileNotFoundException(
                    "$extension (File path for credentials is null or empty. " +
                            "See the `credentialsPath` param description."
                )
            }
            val credentialsFile = File(credentialsFilePath)
            if (!credentialsFile.exists()) {
                throw FileNotFoundException(
                    "$extension (File (${credentialsFile.absolutePath}) " +
                            "with 'client_id' and 'client_secret' for access to Huawei Publish API is not found)"
                )
            }
            CredentialHelper.getCredentials(credentialsFile)
        }
        val clientId = clientIdPriority ?: credentials.value.clientId.nullIfBlank()
        ?: throw IllegalArgumentException(
            "(Huawei credential `clientId` param is null or empty). " +
                    "Please check your credentials file content or as single parameter."
        )
        val clientSecret = clientSecretPriority ?: credentials.value.clientSecret.nullIfBlank()
        ?: throw IllegalArgumentException(
            "(Huawei credential `clientSecret` param is null or empty). " +
                    "Please check your credentials file content or as single parameter."
        )
        return Credentials(clientId, clientSecret)
    }

    @Suppress("ThrowsCount")
    fun getReleasePhaseConfig(): ReleasePhaseConfig? {
        val releasePhaseStartTime = cli.releasePhaseStartTime ?: extension.releasePhase?.startTime
        val releasePhaseEndTime = cli.releasePhaseEndTime ?: extension.releasePhase?.endTime
        val releasePhasePercent = cli.releasePhasePercent?.toDouble() ?: extension.releasePhase?.percent

        val releasePhase =
            if (releasePhaseStartTime != null || releasePhaseEndTime != null || releasePhasePercent != null) {
                if (releasePhaseStartTime == null) {
                    throw IllegalArgumentException(
                        "The `startTime` param must not be null if you choose publishing with Release Phase."
                    )
                }
                if (releasePhaseEndTime == null) {
                    throw IllegalArgumentException(
                        "The `endTime` param must not be null if you choose publishing with Release Phase."
                    )
                }
                if (releasePhasePercent == null) {
                    throw IllegalArgumentException(
                        "The `percent` param must not be null if you choose publishing with Release Phase."
                    )
                }
                ReleasePhaseConfig(
                    startTime = releasePhaseStartTime,
                    endTime = releasePhaseEndTime,
                    percent = releasePhasePercent
                )
            } else {
                null
            }

        if (releasePhase != null) {
            checkReleasePhaseData(releasePhase)
        }
        return releasePhase
    }

    @Suppress("ThrowsCount")
    private fun checkReleasePhaseData(releasePhase: ReleasePhaseConfig) {
        if (releasePhase.percent <= 0 && releasePhase.percent > 100) {
            throw IllegalArgumentException(
                "Wrong percent release phase value = '${releasePhase.percent}'. " +
                    "Allowed values between 0 and 100 with up to two decimal places."
            )
        }

        val nowCalendar = Calendar.getInstance()
        val sdf = SimpleDateFormat(RELEASE_DATE_TIME_FORMAT, Locale.getDefault())

        val endCalendar = Calendar.getInstance()
        endCalendar.time = sdf.parse(releasePhase.endTime)
        if (endCalendar.before(nowCalendar)) {
            throw IllegalArgumentException(
                "Wrong endTime release phase value = '${releasePhase.endTime}'. It less than current moment."
            )
        }

        val startCalendar = Calendar.getInstance()
        startCalendar.time = sdf.parse(releasePhase.startTime)
        if (startCalendar.after(endCalendar)) {
            throw IllegalArgumentException(
                "Wrong startTime release phase value = '${releasePhase.startTime}'. " +
                    "It bigger than endTime = '${releasePhase.endTime}'."
            )
        }
    }

    private fun getReleaseNotesConfig(): ReleaseNotesConfig? {

        val releaseNotePairs = cli.releaseNotes?.split(";")?.map {
            val split = it.split(":")
            split[0] to split[1]
        } ?: extension.releaseNotes?.descriptions?.map {
            it.lang to it.filePath
        }

        if (releaseNotePairs == null) {
            return null
        }

        val removeHtmlTags = cli.removeHtmlTags ?: extension.releaseNotes?.removeHtmlTags ?: false

        val descriptions = releaseNotePairs.map {

            val lang = it.first
            val filePath = it.second

            if (lang.isBlank()) {
                throw IllegalArgumentException(
                    "'lang' param must not be empty."
                )
            }

            val file = releaseNotesFileProvider.getFile(filePath)

            if (!file.exists()) {
                throw IllegalArgumentException(
                    "File '$filePath' with Release Notes for '$lang' language is not exist."
                )
            }

            val newFeatures = if (removeHtmlTags) {
                file.readText(Charsets.UTF_8)
                    // remove html tags
                    .replace("\\<[^>]*>".toRegex(), "")
                    // remove html symbols
                    .replace("(&#)[^;]*;".toRegex(), "*")
                    // compress all non-newline whitespaces to single space
                    .replace("[\\s&&[^\\n]]+".toRegex(), " ")
            } else {
                file.readText(Charsets.UTF_8)
            }

            if (newFeatures.length > RELEASE_NOTES_MAX_LENGTH) {
                throw IllegalArgumentException(
                    "Release notes from '$filePath' for '$lang' language " +
                            "must be less or equals to $RELEASE_NOTES_MAX_LENGTH sign."
                )
            }

            ReleaseNotesDescriptionsConfig(
                lang = lang,
                newFeatures = newFeatures
            )
        }

        return ReleaseNotesConfig(
            descriptions = descriptions,
            removeHtmlTags = removeHtmlTags,
        )
    }

    private fun getAppBasicInfoFile(): File? {
        val path = cli.appBasicInfo ?: extension.appBasicInfo
        if (path.isNullOrBlank()) return null
        val file = File(path)
        if (!file.exists()) {
            throw IllegalArgumentException(
                "File '$path' with AppBasicInfo is not exist."
            )
        }
        return file
    }

    companion object {
        private const val RELEASE_NOTES_MAX_LENGTH = 500
    }
}
