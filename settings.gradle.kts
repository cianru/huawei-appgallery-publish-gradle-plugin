include("plugin")

include(
    ":sample-kotlin",
    ":sample-groovy",
//    ":sample-aar" // For uncomment should get error at sync project time as well;
)

pluginManagement {

    val libsVersionFile = file("gradle/libs.versions.toml")
    val properties = java.util.Properties().apply {
        libsVersionFile.reader().use { load(it) }
    }
    val samplePublishVersion = properties.getProperty("sampleHuaweiPlugin").replace("\"", "")

    resolutionStrategy {
        eachPlugin {
            if(requested.id.namespace == "ru.cian") {
                useModule("ru.cian.huawei-plugin:plugin:${samplePublishVersion}")
            }
        }
    }

    repositories {
        mavenLocal()
        google()
        gradlePluginPortal()
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }
}

plugins {
    id("com.gradle.develocity") version("3.18.2")
}

// To publish report add `-Pscan=true` to build command;
val publishBuildScan = providers.gradleProperty("scan").orNull?.toBoolean() == true
develocity {
    buildScan {
        termsOfUseUrl.set("https://gradle.com/terms-of-service")
        termsOfUseAgree.set("yes")
        if (!publishBuildScan) {
            publishing.onlyIf { false }
        }
    }
}
