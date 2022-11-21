import ru.cian.Dependencies

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.github.ben-manes.versions")
    id("ru.cian.huawei-publish")
}

huaweiPublish {
    instances {
        create("release") {
            credentialsPath = "$projectDir/huawei-credentials.json"
            deployType = ru.cian.huawei.publish.DeployType.DRAFT
            buildFormat = ru.cian.huawei.publish.BuildFormat.AAB
            publishTimeoutMs = 15_000
            publishPeriodMs = 3_000
            releaseTime = "2025-10-21T06:00:00+0300"
            releasePhase = ru.cian.huawei.publish.ReleasePhaseExtension(
                startTime = "2021-10-18T21:00:00+0300",
                endTime = "2025-10-21T06:00:00+0300",
                percent = 1.0
            )
            releaseNotes = listOf(
                ru.cian.huawei.publish.ReleaseNote(
                    lang = "ru-RU",
                    filePath = "$projectDir/release-notes-ru.txt"
                ),
                ru.cian.huawei.publish.ReleaseNote(
                    lang = "en-US",
                    filePath = "$projectDir/release-notes-en.txt"
                )
            )
            appBasicInfo = "$projectDir/app-basic-info.json"
        }
    }
}

android {
    compileSdk = Dependencies.android.compileSdkVersion
    buildToolsVersion = Dependencies.android.buildToolsVersion

    defaultConfig {
        applicationId = "ru.cian.huawei.sample_kotlin"
        minSdk = Dependencies.android.minSdkVersion
        targetSdk = Dependencies.android.targetSdkVersion
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            isDebuggable = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        getByName("debug") {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }
    lint {
        isAbortOnError = false
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

configurations {
    implementation {
        resolutionStrategy.failOnVersionConflict()
    }
}

dependencies {
    implementation(Dependencies.libs.appcompat)
    implementation(Dependencies.libs.kotlinStdlib)
}
