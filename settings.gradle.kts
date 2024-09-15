include("plugin")

include(
    ":sample-kotlin",
    ":sample-groovy",
//    ":sample-aar" // For uncomment should get error at sync project time as well;
)

pluginManagement {

    val huaweiPublish = "1.4.2"

    resolutionStrategy {
        eachPlugin {
            if(requested.id.namespace == "ru.cian") {
                useModule("ru.cian:plugin:${huaweiPublish}")
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
