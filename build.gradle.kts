plugins {
    alias(libs.plugins.benManesVersions)
}

// Delegate lifecycle tasks to the plugin included build so that
// ./gradlew build, ./gradlew check, etc. work from the root.
val pluginBuild = gradle.includedBuild("plugin")

listOf("build", "check", "assemble", "test", "detekt", "publishToMavenLocal", "publish").forEach { taskName ->
    tasks.register(taskName) {
        dependsOn(pluginBuild.task(":$taskName"))
    }
}

tasks.register("buildSamples") {
    description = "Build all sample projects to verify plugin integration"
    group = "verification"

    dependsOn(
        gradle.includedBuild("sample-kotlin").task(":assembleDebug"),
        gradle.includedBuild("sample-groovy").task(":assembleDemoDebug"),
    )
}

tasks.withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate.version) && !isNonStable(currentVersion)
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}