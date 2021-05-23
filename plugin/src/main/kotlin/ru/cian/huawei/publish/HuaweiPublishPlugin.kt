package ru.cian.huawei.publish

import com.android.build.api.variant.ApplicationVariantProperties
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.container
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import ru.cian.huawei.publish.utils.Logger

class HuaweiPublishPlugin : Plugin<Project> {

    override fun apply(project: Project) {

        project.extensions.create(
            HuaweiPublishExtension.MAIN_EXTENSION_NAME,
            HuaweiPublishExtension::class.java,
            project
        )

        project.plugins.withType<AppPlugin> {
            applyInternal(project)
        }

        project.afterEvaluate {
            if (project.plugins.findPlugin(AppPlugin::class.java) == null) {
                project.logger.warn(
                    "The Android Gradle Plugin was not applied. Huawei Publish Plugin " +
                        "will not be configured."
                )
            }
        }
    }

    private fun applyInternal(project: Project) {

        val PLAY_CONFIGS_PATH = "huaweiConfigs"
        val extensionContainer = project.container<HuaweiPublishExtension>()
        val android = project.the<BaseAppModuleExtension>()
        (android as ExtensionAware).extensions.add(PLAY_CONFIGS_PATH, extensionContainer)

        android.onVariants v@{
            project.logger.debug("${Logger.LOG_TAG}: Found variant=${name}, flavor=${flavorName}, buildType=${buildType}")
            onProperties p@{
                if (!debuggable) {
                    createTask(project, this)
                }
            }
        }
    }

    @Suppress("DefaultLocale")
    private fun createTask(project: Project, variant: ApplicationVariantProperties) {
        val variantName = variant.name.capitalize()
        val taskName = "${HuaweiPublishTask.NAME}$variantName"
        project.logger.debug("${Logger.LOG_TAG}: createTask: $taskName")
        project.tasks.create(taskName, HuaweiPublishTask::class.java, variant)
    }
}