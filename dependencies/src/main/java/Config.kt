object config {

    object android {
        const val compileSdkVersion = "30"
        const val targetSdkVersion  = 30
        const val minSdkVersion     = 21
        const val buildToolsVersion = "30.0.2"
    }

    object versions {
        const val kotlin = "1.5.21"
        const val java = "1.8"
        const val junitJupiter = "5.7.0"
    }

    object libs {
        const val appcompat = "androidx.appcompat:appcompat:1.2.0"
        const val kotlinStdlib = "org.jetbrains.kotlin:kotlin-stdlib:${versions.kotlin}"
        const val kotlinReflect = "org.jetbrains.kotlin:kotlin-reflect:${versions.kotlin}"
        const val gson = "com.google.code.gson:gson:2.8.6"
    }

    object junit {
        const val assertk = "com.willowtreeapps.assertk:assertk-jvm:0.23"
        const val hamcreast = "org.hamcrest:hamcrest:2.1"
        const val mockk = "io.mockk:mockk:1.10.3-jdk8"
        const val mockito = "org.mockito:mockito-core:2.23.4"
        const val mockitoKotlin = "com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0"
        const val junitJupiterApi = "org.junit.jupiter:junit-jupiter-api:${versions.junitJupiter}"
        const val junitJupiterEngine = "org.junit.jupiter:junit-jupiter-engine:${versions.junitJupiter}"
        const val junitJupiterParams = "org.junit.jupiter:junit-jupiter-params:${versions.junitJupiter}"
    }

    object gradlePlugins {
        const val gradle = "com.android.tools.build:gradle:4.1.3"
        const val kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${versions.kotlin}"
        const val dokka = "org.jetbrains.dokka:dokka-gradle-plugin:1.4.32"
        const val maven = "com.github.dcendents:android-maven-gradle-plugin:2.1"
        const val bintray = "com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.5"
        const val dependencyVersions = "com.github.ben-manes:gradle-versions-plugin:0.39.0"
        const val publish = "com.gradle.publish:plugin-publish-plugin:0.12.0"
    }

    object sample {
        const val huaweiPlugin = "1.3.0-SNAPSHOT"
    }
}
