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
            releasePhase = ru.cian.huawei.publish.ReleasePhaseExtension(
                startTime = "2021-10-18T21:00:00+0300",
                endTime = "2021-10-21T06:00:00+0300",
                percent = 1.0
            )
        }
    }
}

android {
    compileSdkVersion(Dependencies.android.compileSdkVersion)
    buildToolsVersion(Dependencies.android.buildToolsVersion)

    defaultConfig {
        applicationId = "ru.cian.huawei.sample_kotlin"
        minSdkVersion(Dependencies.android.minSdkVersion)
        targetSdkVersion(Dependencies.android.targetSdkVersion)
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
    "implementation"(Dependencies.libs.appcompat)
}
