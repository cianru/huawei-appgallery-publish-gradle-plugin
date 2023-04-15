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

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "18"
    }
}

configurations.all {
    resolutionStrategy {
        eachDependency {
            if (requested.group == "org.jetbrains.kotlin") {
                useVersion(libs.versions.kotlin.get())
            }
        }
    }
}