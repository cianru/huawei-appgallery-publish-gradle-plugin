package ru.cian.huawei.publish

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariant
import com.android.build.api.variant.VariantSelector
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

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
    private fun createTask(project: Project, variant: ApplicationVariant) {
        val variantName = variant.name.capitalize()
        val publishTaskName = "${HuaweiPublishTask.NAME}$variantName"
        val publishTask = project.tasks.register<HuaweiPublishTask>(publishTaskName, variant)

        project.tasks.whenTaskAdded {
            if (this.name == "assemble$variantName" || this.name == "bundle$variantName") {
                publishTask.get().mustRunAfter(this)
            }
        }
    }
}
