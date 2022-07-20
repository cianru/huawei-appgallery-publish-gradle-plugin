package ru.cian.huawei.publish.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import ru.cian.huawei.publish.BuildFormat
import ru.cian.huawei.publish.Credentials
import ru.cian.huawei.publish.HuaweiPublishCliParam
import ru.cian.huawei.publish.HuaweiPublishConfig
import ru.cian.huawei.publish.HuaweiPublishExtensionConfig
import ru.cian.huawei.publish.ReleasePhaseConfig
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import ru.cian.huawei.publish.models.Credential

internal class ConfigProvider(
    private val extension: HuaweiPublishExtensionConfig,
    private val cli: HuaweiPublishCliParam,
    private val buildFileProvider: BuildFileProvider
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

        val artifactFile = getBuildFile(customBuildFilePath, artifactFormat)

        return HuaweiPublishConfig(
            credentials = credentialsConfig,
            deployType = deployType,
            artifactFormat = artifactFormat,
            artifactFile = artifactFile,
            publishTimeoutMs = publishTimeoutMs,
            publishPeriodMs = publishPeriodMs,
            releaseTime = releaseTime,
            releasePhase = releasePhase
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
                "$artifactFile (No such file or directory). Please run `assemble*` " +
                        "or `bundle*` task to build the application file before current task."
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
            getCredentials(credentialsFile)
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

    fun getReleasePhaseConfig(): ReleasePhaseConfig? {
        val releasePhaseStartTime = cli.releasePhaseStartTime ?: extension.releasePhase?.startTime
        val releasePhaseEndTime = cli.releasePhaseEndTime ?: extension.releasePhase?.endTime
        val releasePhasePercent = cli.releasePhasePercent?.toDouble() ?: extension.releasePhase?.percent
        val releasePhase =
            if (releasePhaseStartTime != null || releasePhaseEndTime != null || releasePhasePercent != null) {
                if (releasePhaseStartTime == null) {
                    throw IllegalArgumentException("The `startTime` param must not be null if you choose publishing with Release Phase.")
                }
                if (releasePhaseEndTime == null) {
                    throw IllegalArgumentException("The `endTime` param must not be null if you choose publishing with Release Phase.")
                }
                if (releasePhasePercent == null) {
                    throw IllegalArgumentException("The `percent` param must not be null if you choose publishing with Release Phase.")
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
            if (releasePhase.percent <= 0 && releasePhase.percent > 100) {
                throw IllegalArgumentException("Wrong percent release phase value = '${releasePhase.percent}'. Allowed values between 0 and 100 with up to two decimal places.")
            }

            val nowCalendar = Calendar.getInstance()
            val sdf = SimpleDateFormat(RELEASE_DATE_TIME_FORMAT, Locale.getDefault())

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
        return releasePhase
    }

    fun getCredentials(credentialsFile: File): Credential {
        val reader = JsonReader(FileReader(credentialsFile.absolutePath))
        val type = object : TypeToken<Credential>() {}.type
        return Gson().fromJson(reader, type)
    }

}