repositories {
    jcenter()
}

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

gradlePlugin {
    // Add fake plugin, if you don't have any
    plugins.register("ru.cian.dependencies-plugin") {
        id = "ru.cian.dependencies-plugin"
        implementationClass = "DependenciesPlugin"
    }
    // Or provide your implemented plugins
}
