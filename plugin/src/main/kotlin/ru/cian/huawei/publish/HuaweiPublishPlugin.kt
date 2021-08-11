package ru.cian.huawei.publish

import com.android.build.api.extension.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariant
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType

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

        project.plugins.withType<AppPlugin> {
            applyInternal(project)
        }

        project.afterEvaluate {
            if (!findAppPlugin(project)) {
                project.logger.warn(
                    "The Android Gradle Plugin was not applied. Huawei Publish Plugin " +
                        "will not be configured."
                )
            }
//            applyInternal(project)
        }
    }

    private fun applyInternal(project: Project) {
        val androidExtension = project.extensions.getByType<ApplicationAndroidComponentsExtension>()
        androidExtension.onVariants v@{ variant ->
            createTask(project, variant)
        }
    }

    @Suppress("DefaultLocale")
    private fun createTask(project: Project, variant: ApplicationVariant) {
        val variantName = variant.name.capitalize()
        val taskName = "${HuaweiPublishTask.NAME}$variantName"
        project.tasks.create(taskName, HuaweiPublishTask::class.java, variant)
    }
}