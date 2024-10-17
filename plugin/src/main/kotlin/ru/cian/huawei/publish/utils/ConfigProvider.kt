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
import ru.cian.huawei.publish.models.Credential
import java.util.Base64

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
        val publishSocketTimeoutInSeconds = cli.publishSocketTimeoutInSeconds?.toLong() ?: extension.publishSocketTimeoutInSeconds
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
            publishSocketTimeoutInSeconds = publishSocketTimeoutInSeconds,
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

        require(artifactFormat.fileExtension == artifactFile.extension) {
            "Build file ${artifactFile.absolutePath} has wrong file extension " +
                "that doesn't match with announced buildFormat($artifactFormat) plugin extension param."
        }
        return artifactFile
    }

    @Suppress("ThrowsCount")
    fun getCredentialsConfig(): Credentials {
        val credentialsBase64 = cli.credentials ?: extension.credentials
        val credentialsFromBase64 = lazy {
            if (credentialsBase64 != null) {
                decodeCredentials(credentialsBase64)
            } else {
                null
            }
        }

        val credentialsFilePath = cli.credentialsPath ?: extension.credentialsPath
        val credentialsResult = lazy {
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
            CredentialHelper.getCredentialsFromFile(credentialsFile)
        }

        val clientId = credentialsFromBase64.value?.clientId
            ?: credentialsResult.value.clientId.nullIfBlank()
            ?: throw IllegalArgumentException(
                "(Huawei credential `clientId` param is null or empty). " +
                    "Please check your credentials file content or as single parameter."
            )
        val clientSecret = credentialsFromBase64.value?.clientSecret
            ?: credentialsResult.value.clientSecret.nullIfBlank()
            ?: throw IllegalArgumentException(
                "(Huawei credential `clientSecret` param is null or empty). " +
                    "Please check your credentials file content or as single parameter."
            )
        return Credentials(clientId, clientSecret)
    }

    @Throws(IllegalArgumentException::class)
    fun decodeCredentials(encodedCredentials: String): Credential {
        val decodedBytes = Base64.getDecoder().decode(encodedCredentials)
        val decodedString = String(decodedBytes, Charsets.UTF_8)
        return CredentialHelper.getCredentialsFromJson(decodedString)
    }

    @Suppress("ThrowsCount")
    fun getReleasePhaseConfig(): ReleasePhaseConfig? {
        val releasePhaseStartTime = cli.releasePhaseStartTime ?: extension.releasePhase?.startTime
        val releasePhaseEndTime = cli.releasePhaseEndTime ?: extension.releasePhase?.endTime
        val releasePhasePercent = cli.releasePhasePercent?.toDouble() ?: extension.releasePhase?.percent

        val releasePhase =
            if (releasePhaseStartTime != null || releasePhaseEndTime != null || releasePhasePercent != null) {
                require(releasePhaseStartTime != null) {
                    "The `startTime` param must not be null if you choose publishing with Release Phase."
                }
                require(releasePhaseEndTime != null) {
                    "The `endTime` param must not be null if you choose publishing with Release Phase."
                }
                require(releasePhasePercent != null) {
                    "The `percent` param must not be null if you choose publishing with Release Phase."
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
        require(releasePhase.percent > 0 && releasePhase.percent <= 100) {
            "Wrong percent release phase value = '${releasePhase.percent}'. " +
                "Allowed values between 1 and 100 with up to two decimal places."
        }

        val nowCalendar = Calendar.getInstance()
        val sdf = SimpleDateFormat(RELEASE_DATE_TIME_FORMAT, Locale.getDefault())

        val endCalendar = Calendar.getInstance()
        endCalendar.time = sdf.parse(releasePhase.endTime)
        require(endCalendar.after(nowCalendar)) {
            "Wrong endTime release phase value = '${releasePhase.endTime}'. It less than current moment."
        }

        val startCalendar = Calendar.getInstance()
        startCalendar.time = sdf.parse(releasePhase.startTime)
        require(startCalendar.before(endCalendar)) {
            "Wrong startTime release phase value = '${releasePhase.startTime}'. " +
                "It bigger than endTime = '${releasePhase.endTime}'."
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

            require(lang.isNotBlank()) {
                "'lang' param must not be empty."
            }

            val file = releaseNotesFileProvider.getFile(filePath)

            require(file.exists()) {
                "File '$filePath' with Release Notes for '$lang' language is not exist."
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

            require(newFeatures.length <= RELEASE_NOTES_MAX_LENGTH) {
                "Release notes from '$filePath' for '$lang' language " +
                    "must be less or equals to $RELEASE_NOTES_MAX_LENGTH sign."
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
        require(file.exists()) {
            "File '$path' with AppBasicInfo is not exist."
        }
        return file
    }

    companion object {

        private const val RELEASE_NOTES_MAX_LENGTH = 500
    }
}
