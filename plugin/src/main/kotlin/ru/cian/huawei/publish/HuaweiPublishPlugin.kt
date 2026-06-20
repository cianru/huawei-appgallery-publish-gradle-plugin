package ru.cian.huawei.publish

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariant
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

class HuaweiPublishPlugin : Plugin<Project> {

    override fun apply(project: Project) {

        project.plugins.withId("com.android.application") {
            configureHuaweiPublish(project)
        }
    }

    private fun configureHuaweiPublish(project: Project) {
        val extension = project.extensions.create<HuaweiPublishExtension>(
            HuaweiPublishExtension.MAIN_EXTENSION_NAME,
            project
        )

        val androidComponents = project.extensions.getByType<ApplicationAndroidComponentsExtension>()
        androidComponents.onVariants { variant ->
            createTask(project, variant, extension)
        }
    }

    private fun createTask(
        project: Project,
        variant: ApplicationVariant,
        extension: HuaweiPublishExtension,
    ) {
        val variantName = variant.name.replaceFirstChar { it.titlecase() }
        val publishTaskName = "${HuaweiPublishTask.TASK_NAME}$variantName"
        val publishTask = project.tasks.register<HuaweiPublishTask>(publishTaskName)
        publishTask.configure {
            description = "Upload and publish application build file " +
                "to Huawei AppGallery Store for ${variant.name} buildType"
            applicationId.set(variant.applicationId)
            this.variantName.set(variant.name)
            apkDirectory.set(variant.artifacts.get(SingleArtifact.APK))
            bundleFile.set(variant.artifacts.get(SingleArtifact.BUNDLE))
            builtArtifactsLoader.set(variant.artifacts.getBuiltArtifactsLoader())
        }
        project.afterEvaluate {
            publishTask.configure {
                extensionConfig = extension.instances.find {
                    it.name.equals(variant.name, ignoreCase = true)
                }
            }
            mustRunAfter(publishTask, "assemble$variantName")
            mustRunAfter(publishTask, "bundle$variantName")
        }
    }

    private fun Project.mustRunAfter(
        publishTask: TaskProvider<HuaweiPublishTask>,
        taskBeforeName: String,
    ) {
        if (tasks.findByName(taskBeforeName) != null) {
            publishTask.configure {
                mustRunAfter(tasks.named(taskBeforeName))
            }
        }
    }
}
