## Pushing a release build to Gradle Plugin Portal

1. Open the plugin directory:
    ```
    cd ./plugin
    ```
1. Edit the `gradle.properties` file:
   Remove `-alpha<number>` from the `VERSION_NAME` and set the version to the release version. For example: `1.0.0`.
1. Upload binaries to Gradle's plugin portal:
   ```bash
   ./gradlew :plugin:publishPlugins
   ```
1. Check uploaded plugin and version at [Gradle Plugin Portal site](https://plugins.gradle.org/plugin/ru.cian.huawei-publish-gradle-plugin).
