## Pushing a build to local repository

1. Open the plugin directory:
    ```
    cd ./plugin
    ```
3. Publish to local repository
   ```bash
   ./gradlew :plugin:publishToMavenLocal
   ```
4. Remove local repository to apply remote build repository
   ```bash
   rm -rv ~/.m2/repository/ru/cian/huawei-publish-gradle-plugin/
   ```
