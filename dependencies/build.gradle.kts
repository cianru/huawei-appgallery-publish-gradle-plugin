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

dependencies {
    implementation ("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.4.21")
    implementation ("org.jetbrains.kotlin:kotlin-reflect:1.4.21")
//    implementation (config.libs.kotlinStdlib)
//    implementation (config.libs.kotlinReflect)
}
