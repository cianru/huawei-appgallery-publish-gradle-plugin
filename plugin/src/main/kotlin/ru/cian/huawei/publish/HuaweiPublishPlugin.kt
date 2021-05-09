package ru.cian.huawei.publish

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.Plugin
import org.gradle.api.Project

class HuaweiPublishPlugin : Plugin<Project> {

    override fun apply(project: Project) {

        project.extensions.create(
            HuaweiPublishExtension.MAIN_EXTENSION_NAME,
            HuaweiPublishExtension::class.java,
            project
        )

        val findAppPlugin = { p: Project ->
            p.plugins.findPlugin(AppPlugin::class.java) != null
        }

        project.afterEvaluate {
            if (!findAppPlugin(project)) {
                project.logger.warn(
                    "The Android Gradle Plugin was not applied. Huawei Publish Plugin " +
                        "will not be configured."
                )
            }
            applyInternal(project)
        }
    }

    private fun applyInternal(project: Project) {
        val androidExtension = project.extensions.getByType(AppExtension::class.java)
        androidExtension.applicationVariants.all { variant ->
            if (!variant.buildType.isDebuggable) {
                createTask(project, variant)
                return@all true
            }
            return@all false
        }
    }

    @Suppress("DefaultLocale")
    private fun createTask(project: Project, variant: BaseVariant) {
        val variantName = variant.name.capitalize()
        val taskName = "${HuaweiPublishTask.NAME}$variantName"
        project.tasks.create(taskName, HuaweiPublishTask::class.java, variant)
    }
}