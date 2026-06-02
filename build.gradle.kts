// buildscript exist here for sample-groovy app;
buildscript {
    repositories {
        google()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:" + libs.versions.androidGradlePlugin.get())
    }
}

plugins {
    alias(libs.plugins.benManesVersions)
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate.version) && !isNonStable(currentVersion)
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}