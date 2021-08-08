includeBuild ("plugin")
includeBuild ("dependencies")

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
    }

    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}