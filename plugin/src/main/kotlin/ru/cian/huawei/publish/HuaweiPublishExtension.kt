package ru.cian.huawei.publish

import groovy.lang.Closure
import org.gradle.api.Project

private const val DEFAULT_PUBLISH_SOCKET_TIMEOUT_IN_SECONDS = 60L
private const val DEFAULT_PUBLISH_TIMEOUT_MS = 10 * 60 * 1000L
private const val DEFAULT_PUBLISH_PERIOD_MS = 15 * 1000L

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

    /**
     * For required property use GradleProperty class instance.
     * For example:
     *  var param by GradleProperty(project, String::class.java)
     */
    var credentials: String? = null
    var credentialsPath: String? = null
    var deployType = DeployType.PUBLISH
    var publishTimeoutMs: Long = DEFAULT_PUBLISH_TIMEOUT_MS
    var publishPeriodMs: Long = DEFAULT_PUBLISH_PERIOD_MS
    var publishSocketTimeoutInSeconds: Long = DEFAULT_PUBLISH_SOCKET_TIMEOUT_IN_SECONDS
    var buildFormat: BuildFormat = BuildFormat.APK
    var buildFile: String? = null
    var releaseTime: String? = null
    var releasePhase: ReleasePhaseExtension? = null
    var releaseNotes: ReleaseNotesExtension? = null
    var appBasicInfo: String? = null

    init {
        require(name.isNotBlank()) {
            "Name must not be blank nor empty"
        }
    }

    fun releasePhase(closure: Closure<ReleasePhaseExtension>): ReleasePhaseExtension {
        releasePhase = ReleasePhaseExtension()
        project.configure(releasePhase!!, closure)
        return releasePhase!!
    }

    fun releaseNotes(closure: Closure<ReleaseNotesExtension>): ReleaseNotesExtension {
        releaseNotes = ReleaseNotesExtension()
        project.configure(releaseNotes!!, closure)
        return releaseNotes!!
    }

    override fun toString(): String {
        return "HuaweiPublishExtensionConfig(" +
            "name='$name', " +
            "credentials='$credentials', " +
            "credentialsPath='$credentialsPath', " +
            "deployType='$deployType', " +
            "publishTimeoutMs='$publishTimeoutMs', " +
            "publishPeriodMs='$publishPeriodMs', " +
            "publishSocketTimeoutInSeconds='$publishSocketTimeoutInSeconds', " +
            "buildFormat='$buildFormat', " +
            "buildFile='$buildFile', " +
            "releaseTime='$releaseTime', " +
            "releasePhase='$releasePhase', " +
            "releaseNotes='$releaseNotes', " +
            "appBasicInfo='$appBasicInfo', " +
            ")"
    }
}

open class ReleasePhaseExtension {

    var startTime: String? = null
    var endTime: String? = null
    var percent: Double? = null

    constructor()

    constructor(startTime: String?, endTime: String?, percent: Double?) {
        this.startTime = startTime
        this.endTime = endTime
        this.percent = percent
    }

    override fun toString(): String {
        return "ReleasePhaseConfig(" +
            "startTime='$startTime', " +
            "endTime='$endTime', " +
            "percent='$percent'" +
            ")"
    }
}

open class ReleaseNotesExtension {

    var descriptions: List<ReleaseNote>? = null
    var removeHtmlTags: Boolean? = null

    constructor()

    constructor(descriptions: List<ReleaseNote>?) {
        this.descriptions = descriptions
    }

    constructor(descriptions: List<ReleaseNote>?, removeHtmlTags: Boolean) {
        this.descriptions = descriptions
        this.removeHtmlTags = removeHtmlTags
    }

    override fun toString(): String {
        return "ReleaseNotesExtension(" +
            "descriptions='$descriptions', " +
            "removeHtmlTags='$removeHtmlTags'" +
            ")"
    }
}

open class ReleaseNote {

    lateinit var lang: String
    lateinit var filePath: String

    constructor()

    constructor(lang: String, filePath: String) {
        this.lang = lang
        this.filePath = filePath
    }

    override fun toString(): String {
        return "ReleaseNote(" +
            "lang='$lang', " +
            "filePath='$filePath'" +
            ")"
    }
}

enum class BuildFormat(val fileExtension: String) {
    APK("apk"),
    AAB("aab"),
}

enum class DeployType {
    /**
     * Deploy without draft saving and submit on users;
     */
    UPLOAD_ONLY,

    /**
     * Deploy and save as draft without submit on users;
     */
    DRAFT,

    /**
     * Deploy, save as draft and submit build on users;
     */
    PUBLISH,
}
