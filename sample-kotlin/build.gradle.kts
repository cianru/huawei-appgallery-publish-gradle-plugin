plugins {
    id("com.android.application")
    id("kotlin-android")
    id("ru.cian.huawei-publish")
}

huaweiPublish {
    instances {
        create("release") {
            credentials = "ewogICJjbGllbnRfaWQiOiAiPENMSUVOVF9JRF9CQVNFNjQ+IiwKICAiY2xpZW50X3NlY3JldCI6ICI8Q0xJRU5UX1NFQ1JFVF9CQVNFNjQ+Igp9"
//            credentialsPath = "$projectDir/huawei-credentials.json"
            deployType = ru.cian.huawei.publish.DeployType.DRAFT
            buildFormat = ru.cian.huawei.publish.BuildFormat.AAB
            publishSocketTimeoutInSeconds = 60
            publishTimeoutMs = 15_000
            publishPeriodMs = 3_000
            releaseTime = "2025-10-21T06:00:00+0300"
            releasePhase = ru.cian.huawei.publish.ReleasePhaseExtension(
                startTime = "2021-10-18T21:00:00+0300",
                endTime = "2025-10-21T06:00:00+0300",
                percent = 1.0
            )
            releaseNotes = ru.cian.huawei.publish.ReleaseNotesExtension(
                descriptions = listOf(
                    ru.cian.huawei.publish.ReleaseNote(
                        lang = "ru-RU",
                        filePath = "$projectDir/release-notes-ru.txt"
                    ),
                    ru.cian.huawei.publish.ReleaseNote(
                        lang = "en-US",
                        filePath = "$projectDir/release-notes-en.txt"
                    )
                ),
                removeHtmlTags = true
            )
            appBasicInfo = "$projectDir/app-basic-info.json"
        }
    }
}

android {
    compileSdk = libs.versions.compileSdkVersion.get().toInt()

    namespace = "ru.cian.huawei.sample.kotlin"

    defaultConfig {
        applicationId = "ru.cian.huawei.sample.kotlin"
        minSdk = libs.versions.minSdkVersion.get().toInt()
        targetSdk = libs.versions.targetSdkVersion.get().toInt()
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

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.jvm.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.jvm.get())
    }
}

configurations {
    implementation {
        resolutionStrategy.failOnVersionConflict()
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(platform(libs.kotlinBom))
}
