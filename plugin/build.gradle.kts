plugins {
    `kotlin-dsl`
    `maven-publish`
    `signing`
    id("com.github.ben-manes.versions")
    id("org.jetbrains.dokka")
    id("com.jfrog.bintray")
    id("com.gradle.plugin-publish")
    id("ru.cian.dependencies-plugin")
}

apply(from = "$rootDir/config/maven-publish.gradle")
apply(from = "$rootDir/config/bintray-publish.gradle")
apply(from = "$rootDir/config/gradle-portal.gradle")
apply(from = "$rootDir/../dependencies.gradle")

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<org.jetbrains.dokka.gradle.DokkaTask>().configureEach {
    outputDirectory.set(buildDir.resolve("dokka"))
}

repositories {
    google()
    mavenCentral()
    mavenLocal()
    maven { url = uri("https://plugins.gradle.org/m2/") }
}

dependencies {
    implementation(config.libs.kotlinStdlib)
    implementation(config.libs.kotlinReflect)
    implementation(config.libs.gson)

    compileOnly(config.gradlePlugins.gradle)

    testImplementation(config.junit.junitJupiterApi)
    testImplementation(config.junit.junitJupiterEngine)
    testImplementation(config.junit.junitJupiterParams)
    testImplementation(config.junit.mockk)
    testImplementation(config.junit.mockito)
    testImplementation(config.junit.mockitoKotlin)
    testImplementation(config.junit.hamcreast)
    testImplementation(config.junit.assertk)
}
