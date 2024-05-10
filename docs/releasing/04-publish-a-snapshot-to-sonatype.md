## Pushing a SNAPSHOT build to Sonatype

1. Open the plugin directory:
    ```
    cd ./plugin
    ```
2. Edit the `gradle.properties` file:
   ```bash
   IS_SNAPSHOT=true
   ```
3. Upload binaries to Sonatype:
   ```bash
   ./gradlew :plugin:publishHuaweiPublicationToMavenRepository
   ```
4. Check snapshot: [nexus-search](https://oss.sonatype.org/#nexus-search;quick~ru.cian)
