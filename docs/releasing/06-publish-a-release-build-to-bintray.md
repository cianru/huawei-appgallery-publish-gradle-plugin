## Pushing a release build to Bintray

1. Open the plugin directory:
    ```
    cd ./plugin
    ```
1. Edit the `gradle.properties` file:
   ```bash
   IS_SNAPSHOT=false
   ```
1. Upload binaries to Bintray:
   ```bash
   ./gradlew build :plugin:bintrayUpload
   ```
1. Check uploaded files and version Bintray site: [bintray.com](https://bintray.com/myumatov/ru.cian/huawei-publish-gradle-plugin)   
