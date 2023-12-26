plugins {
    `kotlin-dsl`
    `maven-publish`
    `signing`
    alias(libs.plugins.detekt)
    alias(libs.plugins.pluginPublish)
    alias(libs.plugins.bintray)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlinJvm)
}

apply(from = "$projectDir/config/check-jdk.gradle")
apply(from = "$projectDir/config/maven-publish.gradle")
apply(from = "$projectDir/config/bintray-publish.gradle")
apply(from = "$projectDir/config/gradle-portal.gradle")

detekt {

    // The directories where detekt looks for source files.
    // Defaults to `files("src/main/java", "src/test/java", "src/main/kotlin", "src/test/kotlin")`.
    source = files("src/main/java", "src/main/kotlin")

    // Builds the AST in parallel. Rules are always executed in parallel.
    // Can lead to speedups in larger projects. `false` by default.
    parallel = false

    // Define the detekt configuration(s) you want to use.
    // Defaults to the default detekt configuration.
    config = files("$projectDir/config/detekt/detekt-config.yml")

    // A way of suppressing issues before introducing detekt.
    baseline = file("$projectDir/config/detekt/detekt-baseline.xml")

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

repositories {
    google()
    mavenCentral()
    mavenLocal()
    maven { url = uri("https://plugins.gradle.org/m2/") }
}

dependencies {
    implementation(platform(libs.kotlinBom))
    implementation(libs.gson)
    implementation(libs.okHttp)
    compileOnly(libs.androidGradlePlugin)

    detektPlugins(libs.detektFormating)
    detektPlugins(libs.detektLibraries)

    testImplementation(libs.test.junitJupiterApi)
    testImplementation(libs.test.junitJupiterEngine)
    testImplementation(libs.test.junitJupiterParams)
    testImplementation(libs.test.mockk)
    testImplementation(libs.test.mockitoCore)
    testImplementation(libs.test.mockitoKotlin)
    testImplementation(libs.test.hamcreast)
    testImplementation(libs.test.assertk)
    testImplementation(libs.androidGradlePlugin)
}
