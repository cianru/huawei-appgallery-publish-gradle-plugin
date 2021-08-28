include("plugin")

include(
    ":sample-kotlin",
    ":sample-groovy"
//    ":sample-aar" // For uncomment should get error at sync project time as well;
)

pluginManagement {

    val huaweiPublish = "1.3.0-SNAPSHOT"

    resolutionStrategy {
        eachPlugin {
            if(requested.id.namespace == "ru.cian") {
                useModule("ru.cian:huawei-publish-gradle-plugin:${huaweiPublish}")
            }
        }
    }

    plugins {
        id("ru.cian.huawei-publish") version huaweiPublish apply false
        id("com.android.tools.build") version "7.0.0" apply false
        id("org.jetbrains.dokka") version "1.4.30" apply false
        id("com.github.dcendents") version "plugin:2.1" apply false
        id("com.jfrog.bintray") version "1.8.5" apply false
        id("com.gradle.plugin-publish") version "0.14.0" apply false
        id("org.jetbrains.kotlin.jvm") version "1.5.21" apply false
        id("com.github.ben-manes.versions") version "0.39.0" apply false
    }

    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
        maven { url = uri("https://plugins.gradle.org/m2/") }
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
    }
}