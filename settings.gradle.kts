rootProject.name = "Huawei Publish Gradle Plugin"

includeBuild (
    "plugin"
)

include(
    ":sample1",
    ":sample2"
//    ":sample-aar" // For uncomment should get error at sync project time as well;
)

pluginManagement {

    val huaweiPublish = "1.2.4-SNAPSHOT"

    resolutionStrategy {
        eachPlugin {
            if(requested.id.namespace == "ru.cian") {
                useModule("ru.cian:huawei-publish-gradle-plugin:${huaweiPublish}")
            }
        }
    }

    plugins {
        id("com.ofg.uptodate") version "1.6.3" apply false
        id("ru.cian.huawei-publish") version huaweiPublish apply false
    }

    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        jcenter()
    }

}