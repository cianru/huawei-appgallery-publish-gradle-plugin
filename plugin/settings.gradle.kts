pluginManagement {
    repositories {
        mavenLocal()
        google()
        gradlePluginPortal()
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "plugin"
