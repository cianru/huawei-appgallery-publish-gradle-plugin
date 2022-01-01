import ru.cian.Dependencies
// buildscript exist here for sample-groovy app;
buildscript {

    repositories {
        google()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.0.4")
    }
}

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("com.github.ben-manes.versions")
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
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
                useVersion(Dependencies.versions.kotlin)
            }
        }
    }
}

dependencies {
    implementation(Dependencies.libs.kotlinStdlib)
    implementation(Dependencies.libs.kotlinReflect)
    implementation(Dependencies.libs.gson)
    implementation(Dependencies.libs.okHttp)

    compileOnly(Dependencies.gradlePlugins.gradle)
}
