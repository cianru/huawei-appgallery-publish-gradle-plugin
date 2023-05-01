include("plugin")

include(
    ":sample-kotlin",
    ":sample-groovy",
//    ":sample-aar" // For uncomment should get error at sync project time as well;
)

pluginManagement {

    val huaweiPublish = "1.4.0-SNAPSHOT"

    resolutionStrategy {
        eachPlugin {
            if(requested.id.namespace == "ru.cian") {
                useModule("ru.cian:huawei-publish-gradle-plugin:${huaweiPublish}")
            }
        }
    }

    repositories {
        mavenLocal()
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
        google()
        gradlePluginPortal()
        mavenCentral()
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }
}