plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.github.ben-manes.versions")
    id("ru.cian.huawei-publish")
    id("ru.cian.dependencies-plugin")
}

apply(from = "$rootDir/dependencies.gradle")

huaweiPublish {
    instances {
        create("release") {
            credentialsPath = "$rootDir/sample1/huawei-credentials.json"
            deployType = ru.cian.huawei.publish.DeployType.DRAFT
            releasePhase = ru.cian.huawei.publish.ReleasePhaseExtension(
                startTime = "2021-10-18T21:00:00+0300",
                endTime = "2021-10-21T06:00:00+0300",
                percent = 1.0
            )
        }
    }
}

android {
    compileSdkVersion = config.android.compileSdkVersion
    buildToolsVersion = config.android.buildToolsVersion

    defaultConfig {
        applicationId = "ru.cian.huawei.sample1"
        minSdkVersion(config.android.minSdkVersion)
        targetSdkVersion(config.android.targetSdkVersion)
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            minifyEnabled(false)
            debuggable(false)
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        getByName("debug") {
            debuggable(true)
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }
    lintOptions {
        isAbortOnError = false
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

configurations {
    implementation {
        resolutionStrategy.failOnVersionConflict()
    }
}

dependencies {
    implementation (config.libs.appcompat)
}
