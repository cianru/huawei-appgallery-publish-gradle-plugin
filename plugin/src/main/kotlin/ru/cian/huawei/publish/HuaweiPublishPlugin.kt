package ru.cian.huawei.publish

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Variant
import com.android.build.gradle.api.AndroidBasePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class HuaweiPublishPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.plugins.withType(AndroidBasePlugin::class.java) {
            configureHuaweiPublish(project)
        }
    }

    private fun configureHuaweiPublish(project: Project) {
        project.extensions.create(
            HuaweiPublishExtension.MAIN_EXTENSION_NAME,
            HuaweiPublishExtension::class.java,
            project
        )

        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponents.onVariants(androidComponents.selector().withBuildType("release")) { variant ->
            createTask(project, variant)
        }
    }

    @Suppress("DefaultLocale")
    private fun createTask(project: Project, variant: Variant) {
        val variantName = variant.name.capitalize()
        val taskName = "${HuaweiPublishTask.NAME}$variantName"
        project.tasks.create(taskName, HuaweiPublishTask::class.java, variant)
    }
}