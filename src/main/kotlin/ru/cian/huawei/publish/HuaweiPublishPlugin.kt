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

        project.afterEvaluate {
            if (!project.plugins.hasPlugin(AppPlugin::class.java)) {
                throw IllegalStateException(
                    "The Android Gradle Plugin was not applied. Huawei publish will not be configured."
                )
            }
            project.extensions.getByType(AppExtension::class.java)
                .applicationVariants.all { variant ->
                    if (!variant.buildType.isDebuggable) {
                        createTask(project, variant)
                    }
                }
        }
    }

    @Suppress("DefaultLocale")
    private fun createTask(project: Project, variant: BaseVariant) {
        val variantName = variant.name.capitalize()
        val taskName = "${HuaweiPublishTask.NAME}$variantName"
        project.tasks.create(taskName, HuaweiPublishTask::class.java, variant)
    }
}