## Pushing a release build to Sonatype

1. Open the plugin directory:
    ```
    cd ./plugin
    ```
1. Edit the `gradle.properties` file:
   ```bash
   IS_SNAPSHOT=false
   ```
1. Edit `README.md` so that Gradle examples point to the new version
1. Edit changelog, add relevant changes, note the date and new version (follow the existing pattern)
1. Verify that the everything works:
   ```bash
   ./gradlew clean check
   ```
1. Verify that the code style is correct:
   ```bash
   ./gradlew detekt
   ```
1. Upload binaries to Sonatype:
   ```bash
   ./gradlew :plugin:publishHuaweiPublicationToMavenRepository
   ```
1. Check uploaded files and version Sonatype site: [search.maven.org](https://search.maven.org/search?q=ru.cian)
   and [repo1.maven.org](https://repo1.maven.org/maven2/ru/cian/huawei-publish-gradle-plugin/)
1. Go to [oss.sonatype.org](https://oss.sonatype.org), log in with your credentials
1. Click "Staging Repositories"
1. Find the "ru.cian" repo, usually at the bottom of the list
1. "Close" the repository (select it then click the "close" button up top), the text field doesn't matter so put whatever you want in it
1. Wait until that's done
1. "Release" the repository, leave the checkbox "Automatically Drop" checked. Yeap, we're in Maven Central now!
