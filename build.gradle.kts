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
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.benManesVersions)
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

//kotlin {
//    jvmToolchain(libs.versions.jvm.get().toInt())
//}
//
//java {
//    toolchain {
//        languageVersion.set(JavaLanguageVersion.of(libs.versions.jvm.get()))
//    }
//}

configurations.all {
    resolutionStrategy {
        eachDependency {
            if (requested.group == "org.jetbrains.kotlin") {
                useVersion(libs.versions.kotlin.get())
            }
        }
    }
}

tasks.withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate.version) && !isNonStable(currentVersion)
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}