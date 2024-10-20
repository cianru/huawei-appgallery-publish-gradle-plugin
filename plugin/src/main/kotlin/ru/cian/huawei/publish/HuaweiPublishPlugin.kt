package ru.cian.huawei.publish

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariant
import com.android.build.api.variant.VariantSelector
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import java.io.File

class HuaweiPublishPlugin : Plugin<Project> {

    override fun apply(project: Project) {

        project.plugins.withType<AppPlugin> {
            configureHuaweiPublish(project)
        }
    }

    private fun configureHuaweiPublish(project: Project) {
        project.extensions.create<HuaweiPublishExtension>(
            HuaweiPublishExtension.MAIN_EXTENSION_NAME,
            project
        )

        val androidComponents = project.extensions.getByType<ApplicationAndroidComponentsExtension>()
        androidComponents.onVariants(androidComponents.selector().all() as VariantSelector) { variant ->
            createTask(project, variant)
        }
    }

    @Suppress("DefaultLocale")
    private fun createTask(
        project: Project,
        variant: ApplicationVariant,
    ) {
        val variantName = variant.name.capitalize()
        val publishTaskName = "${HuaweiPublishTask.TASK_NAME}$variantName"
        val publishTask = project.tasks.register<HuaweiPublishTask>(publishTaskName, variant)
        scheduleTasksOrder(publishTask, project, variantName)
//
//
//        val variantName = variant.name
//        val variantApplicationId = variant.applicationId.get()
//        val variantApkBuildFilePath = getFinalApkArtifactCompat(variant).singleOrNull()?.absolutePath
//        val variantAabBuildFilePath = getFinalBundleArtifactCompat(variant).singleOrNull()?.absolutePath
// //        val variantApkBuildFilePath = project.rootProject.path + "/app/build/outputs/bundle/release/app-release.aab"
// //        val variantAabBuildFilePath = project.rootProject.path + "/app/build/outputs/apk/release/app-release.apk"
//        val publishTaskName = "${HuaweiPublishTask.TASK_NAME}${variantName.capitalize()}"
//        val publishTask = project.tasks.register<HuaweiPublishTask>(
//            publishTaskName,
//            variantApplicationId,
//            variantName,
//            Optional.ofNullable(variantApkBuildFilePath),
//            Optional.ofNullable(variantAabBuildFilePath),
//        )
// //        scheduleTasksOrder(publishTask, project, variantName)
    }

    private fun scheduleTasksOrder(
        publishTask: TaskProvider<HuaweiPublishTask>,
        project: Project,
        variantName: String
    ) {
        project.gradle.projectsEvaluated {
            mustRunAfter(project, publishTask, "assemble$variantName")
            mustRunAfter(project, publishTask, "bundle$variantName")
        }
    }

    private fun mustRunAfter(
        project: Project,
        publishTask: TaskProvider<HuaweiPublishTask>,
        taskBeforeName: String,
    ) {
        if (project.tasks.findByName(taskBeforeName) != null) {
            val assembleTask = project.tasks.named(taskBeforeName).get()
            publishTask.get().mustRunAfter(assembleTask)
        }
    }

    // TODO(a.mirko): Remove after https://github.com/gradle/gradle/issues/16777
    // TODO(a.mirko): Remove after https://github.com/gradle/gradle/issues/16775
    @SuppressWarnings("UnusedPrivateMember")
    private fun getFinalApkArtifactCompat(variant: ApplicationVariant): List<File> {
        val apkDirectory = variant.artifacts.get(SingleArtifact.APK).get()
        return variant.artifacts.getBuiltArtifactsLoader().load(apkDirectory)
            ?.elements?.map { element -> File(element.outputFile) }
            ?: apkDirectory.asFileTree.matching { include("*.apk") }.map { it.absolutePath }.map { File(it) }
            ?: emptyList()
    }

    @SuppressWarnings("UnusedPrivateMember")
    private fun getFinalBundleArtifactCompat(variant: ApplicationVariant): List<File> {
        val aabFile = variant.artifacts.get(SingleArtifact.BUNDLE).get().asFile
        return listOf(aabFile)
    }
}
