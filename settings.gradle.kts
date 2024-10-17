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
    id("com.gradle.enterprise") version("3.13.2")
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
        // To publish report add `-Pscan` to build command;
        publishAlwaysIf(settings.extra.has("scan"))
    }
}
