package ru.cian.huawei.publish

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
        project.extensions.create<HuaweiPublishExtension>(
            HuaweiPublishExtension.MAIN_EXTENSION_NAME,
            project
        )

        val androidComponents = project.extensions.getByType<ApplicationAndroidComponentsExtension>()
        androidComponents.onVariants { variant ->
            createTask(project, variant)
        }
    }

    private fun createTask(
        project: Project,
        variant: ApplicationVariant,
    ) {
        val variantName = variant.name.replaceFirstChar { it.titlecase() }
        val publishTaskName = "${HuaweiPublishTask.TASK_NAME}$variantName"
        val publishTask = project.tasks.register<HuaweiPublishTask>(publishTaskName, variant)
        scheduleTasksOrder(publishTask, project, variantName)
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
}
