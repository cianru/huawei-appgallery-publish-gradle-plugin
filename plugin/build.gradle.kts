import ru.cian.Dependencies

plugins {
    `kotlin-dsl`
    `maven-publish`
    `signing`
    id("com.github.ben-manes.versions")
    id("org.jetbrains.dokka")
    id("com.jfrog.bintray")
    id("com.gradle.plugin-publish")
}

apply(from = "$projectDir/config/maven-publish.gradle")
apply(from = "$projectDir/config/bintray-publish.gradle")
apply(from = "$projectDir/config/gradle-portal.gradle")

tasks.withType<org.jetbrains.dokka.gradle.DokkaTask>().configureEach {
    outputDirectory.set(buildDir.resolve("dokka"))
}

tasks.withType<Test> {
    useJUnitPlatform {}
}

repositories {
    google()
    mavenCentral()
    mavenLocal()
    maven { url = uri("https://plugins.gradle.org/m2/") }
}

dependencies {
    implementation(Dependencies.libs.kotlinStdlib)
    implementation(Dependencies.libs.kotlinReflect)
    implementation(Dependencies.libs.gson)
    implementation(Dependencies.libs.okHttp)

    compileOnly(Dependencies.gradlePlugins.gradle)

    testImplementation(Dependencies.junit.junitJupiterApi)
    testImplementation(Dependencies.junit.junitJupiterEngine)
    testImplementation(Dependencies.junit.junitJupiterParams)
    testImplementation(Dependencies.junit.mockk)
    testImplementation(Dependencies.junit.mockito)
    testImplementation(Dependencies.junit.mockitoKotlin)
    testImplementation(Dependencies.junit.hamcreast)
    testImplementation(Dependencies.junit.assertk)
    testImplementation(Dependencies.gradlePlugins.gradle)
}
