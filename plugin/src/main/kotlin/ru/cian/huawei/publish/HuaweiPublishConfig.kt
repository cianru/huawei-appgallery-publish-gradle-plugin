package ru.cian.huawei.publish

import java.io.File

internal data class HuaweiPublishConfig(
    val credentials: Credentials,
    val deployType: DeployType,
    val artifactFormat: BuildFormat,
    val artifactFile: File,
    val publishTimeoutMs: Long,
    val publishPeriodMs: Long,
    val releaseTime: String?,
    val releasePhase: ReleasePhaseConfig?,
    val releaseNotes: ReleaseNotesConfig?,
    val appBasicInfoFile: File?
)

internal data class ReleasePhaseConfig(
    val startTime: String,
    val endTime: String,
    val percent: Double,
)

internal data class Credentials(
    var clientId: String,
    var clientSecret: String,
)

internal data class HuaweiPublishCliParam(
    val deployType: DeployType? = null,
    val publishTimeoutMs: String? = null,
    val publishPeriodMs: String? = null,
    val credentialsPath: String? = null,
    val clientId: String? = null,
    val clientSecret: String? = null,
    val buildFormat: BuildFormat? = null,
    val buildFile: String? = null,
    val releaseTime: String? = null,
    val releasePhaseStartTime: String? = null,
    val releasePhaseEndTime: String? = null,
    val releasePhasePercent: String? = null,
    val releaseNotes: String? = null,
    val removeHtmlTags: Boolean? = null,
    val apiStub: Boolean? = null,
    val appBasicInfo: String? = null,
)

internal data class ReleaseNotesConfig(
    val languages: List<ReleaseNotesLanguagesConfig>?,
    val removeHtmlTags: Boolean,
)
internal data class ReleaseNotesLanguagesConfig(
    val lang: String,
    val newFeatures: String,
)