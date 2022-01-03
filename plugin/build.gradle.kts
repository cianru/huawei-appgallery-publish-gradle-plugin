import ru.cian.Dependencies

plugins {
    `kotlin-dsl`
    `maven-publish`
    `signing`
    id("com.github.ben-manes.versions")
    id("org.jetbrains.dokka")
    id("com.jfrog.bintray")
    id("com.gradle.plugin-publish")
    id("io.gitlab.arturbosch.detekt")
}

apply(from = "$projectDir/config/maven-publish.gradle")
apply(from = "$projectDir/config/bintray-publish.gradle")
apply(from = "$projectDir/config/gradle-portal.gradle")

detekt {
    // Version of Detekt that will be used. When unspecified the latest detekt
    // version found will be used. Override to stay on the same version.
    toolVersion = "1.19.0-RC1"

    // The directories where detekt looks for source files.
    // Defaults to `files("src/main/java", "src/test/java", "src/main/kotlin", "src/test/kotlin")`.
    source = files("src/main/java", "src/main/kotlin")

    // Builds the AST in parallel. Rules are always executed in parallel.
    // Can lead to speedups in larger projects. `false` by default.
    parallel = false

    // Define the detekt configuration(s) you want to use.
    // Defaults to the default detekt configuration.
    config = files("$projectDir/config/detekt/detekt-config.yml")

    // Applies the config files on top of detekt's default config file. `false` by default.
    buildUponDefaultConfig = true

    // Turns on all the rules. `false` by default.
    allRules = false

    // Specifying a baseline file. All findings stored in this file in subsequent runs of detekt.
//    baseline = file("$projectDir/config/baseline.xml")

    // Disables all default detekt rulesets and will only run detekt with custom rules
    // defined in plugins passed in with `detektPlugins` configuration. `false` by default.
    disableDefaultRuleSets = false

    // Adds debug output during task execution. `false` by default.
    debug = false

    // If set to `true` the build does not fail when the
    // maxIssues count was reached. Defaults to `false`.
    ignoreFailures = false
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    reports {
        html {
            required.set(true)
            outputLocation.set(file("build/reports/detekt.html"))
        }
        xml.required.set(false)
        txt.required.set(false)
        sarif.required.set(false)
    }
}

tasks.withType<org.jetbrains.dokka.gradle.DokkaTask>().configureEach {
    outputDirectory.set(buildDir.resolve("dokka"))
}

tasks.withType<Test> {
    useJUnitPlatform {}
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
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
