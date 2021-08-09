import ru.cian.Dependencies
// buildscript exist here for sample-groovy app;
buildscript {

    repositories {
        google()
    }

    dependencies {
//        classpath(Dependencies.gradlePlugins.gradle)
        classpath("com.android.tools.build:gradle:4.1.3")
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
    "implementation" (Dependencies.libs.kotlinStdlib)
    "implementation" (Dependencies.libs.kotlinReflect)
    "implementation" (Dependencies.libs.gson)

    "compileOnly" (Dependencies.gradlePlugins.gradle)
}

//tasks.register("clean", Delete::class) {
//    delete(rootProject.buildDir)
//}
