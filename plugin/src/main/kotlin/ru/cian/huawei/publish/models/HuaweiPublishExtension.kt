package ru.cian.huawei.publish

import groovy.lang.Closure
import org.gradle.api.Project

open class HuaweiPublishExtension(
        project: Project
) {

    val instances = project.container(HuaweiPublishExtensionConfig::class.java) { name ->
        HuaweiPublishExtensionConfig(name, project)
    }

    companion object {
        const val MAIN_EXTENSION_NAME = "huaweiPublish"
    }
}

class HuaweiPublishExtensionConfig(
    val name: String,
    val project: Project
) {

    init {
        if (name.isBlank()) {
            throw IllegalArgumentException("Name must not be blank nor empty")
        }
    }

    fun releasePhase(closure: Closure<ReleasePhaseExtension>): ReleasePhaseExtension {
        releasePhase = ReleasePhaseExtension()
        project.configure(releasePhase, closure)
        return releasePhase!!
    }

/*
    For required property use GradleProperty class instance. For example:
    var credentialsPath by GradleProperty(project, String::class.java)
 */
    var credentialsPath: String? = null
    var clientId: String? = null
    var clientSecret: String? = null
    var publish: Boolean = true
    var publishTimeoutMs: Long? = null
    var publishPeriodMs: Long? = null
    var buildFormat: BuildFormat = BuildFormat.APK
    var buildFile: String? = null
    var releaseTime: String? = null
    var releasePhase: ReleasePhaseExtension? = null

    override fun toString(): String {
        return "HuaweiPublishCredential(" +
                "name='$name', " +
                "credentialsPath='$credentialsPath', " +
                "clientId='$clientId', " +
                "clientSecret='$clientSecret', " +
                "publish='$publish', " +
                "publishTimeoutMs='$publishTimeoutMs', " +
                "publishPeriodMs='$publishPeriodMs', " +
                "buildFormat='$buildFormat', " +
                "buildFile='$buildFile', " +
                "releaseTime='$releaseTime', " +
                "releasePhase='$releasePhase'" +
                ")"
    }
}

open class ReleasePhaseExtension {

    var startTime: String? = null
    var endTime: String? = null
    var percent: Double? = null

    override fun toString(): String {
        return "ReleasePhaseConfig(" +
                "startTime='$startTime', " +
                "endTime='$endTime', " +
                "percent='$percent'" +
                ")"
    }
}

enum class BuildFormat(val fileExtension: String) {
    APK("apk"),
    AAB("aab")
}
