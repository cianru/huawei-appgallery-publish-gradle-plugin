package ru.cian.huawei.publish

import java.io.File

internal data class HuaweiPublishConfig(
    val credentials: Credentials,
    val publish: Boolean = true,
    val artifactFormat: BuildFormat = BuildFormat.APK,
    val artifactFile: File,
    val publishTimeoutMs: Long,
    val publishPeriodMs: Long,
    val releaseTime: String? = null,
    val releasePhase: ReleasePhaseConfig? = null
)

internal data class ReleasePhaseConfig(
    val startTime: String,
    val endTime: String,
    val percent: Double
)

internal data class Credentials(
    var clientId: String,
    var clientSecret: String
)

internal data class HuaweiPublishCliParam(
    val noPublish: Boolean?,
    val publish: Boolean?,
    val publishTimeoutMs: String?,
    val publishPeriodMs: String?,
    val credentialsPath: String?,
    val clientId: String?,
    val clientSecret: String?,
    val buildFormat: BuildFormat?,
    val buildFile: String?,
    val releaseTime: String?,
    val releasePhaseStartTime: String?,
    val releasePhaseEndTime: String?,
    val releasePhasePercent: String?,
    val apiStub: Boolean?
)

