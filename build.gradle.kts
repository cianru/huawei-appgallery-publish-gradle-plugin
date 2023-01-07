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

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
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

dependencies {
    implementation(libs.kotlinStdlib)
    implementation(libs.kotlinReflect)
    implementation(libs.gson)
    implementation(libs.okHttp)
}
