## Pushing a SNAPSHOT build to local repository

1. Open the plugin directory:
    ```
    cd ./plugin
    ```
2. Edit the `gradle.properties` file:
   ```bash
   IS_SNAPSHOT=true
   ```
3. Publish to local repository
   ```bash
   ./gradlew publishToMavenLocal
   ```
4. Remove local repository to apply remote build repository
   ```bash
   rm -rv ~/.m2/repository/ru/cian/huawei-publish-gradle-plugin/
   ```
