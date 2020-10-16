package ru.cian.huawei.publish

import groovy.lang.Closure
import org.gradle.api.Project
import ru.cian.huawei.publish.utils.GradleProperty

open class HuaweiPublishExtension(
        project: Project
) {

    val instances = project.container(HuaweiPublishConfig::class.java) { name ->
        HuaweiPublishConfig(name, project)
    }

    companion object {
        const val MAIN_EXTENSION_NAME = "huaweiPublish"
    }
}

class HuaweiPublishConfig(
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

    var credentialsPath by GradleProperty(project, String::class.java)
    var publish: Boolean = true
    var buildFormat: BuildFormat = BuildFormat.APK
    var buildFile: String? = null
    var releaseTime: String? = null
    var releasePhase: ReleasePhaseExtension? = null

    override fun toString(): String {
        return "HuaweiPublishCredential(" +
                "name='$name', " +
                "credentialsPath='$credentialsPath', " +
                "publish='$publish', " +
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
