How To Release
==============

Due to Maven Central's very particular requirements, the release process is a bit
elaborate and requires a good deal of local configuration.  This guide should walk
you through it.  It won't do anyone outside of KeepSafe any good, but the workflow
is representative of just about any project deploying via Sonatype.

We currently deploy to both Maven Central (via Sonatype's OSS Nexus instance) and to
plugins.gradle.org.

########################################################################
### Prerequisites
########################################################################

1. A *published* GPG code-signing key
1. A Sonatype Nexus OSS account with permission to publish in ru.cian
1. A plugins.gradle.org account with permission to publish in ru.cian
1. Permission to push directly to https://github.com/cianru/huawei-publish-gradle-plugin

########################################################################
### Setup
########################################################################

1. Add your GPG key to your github profile - this is required
   for github to know that your commits and tags are "verified".
1. Configure your code-signing key in ~/.gradle.properties:
   ```gradle
   signing.keyId=<key ID of your GPG signing key>
   signing.password=<your key's passphrase>
   signing.secretKeyRingFile=/path/to/your/secring.gpg
   ```
1. Configure your Sonatype credentials in ~/.gradle/gradle.properties:
   ```gradle
   SONATYPE_NEXUS_USERNAME=<nexus username>
   SONATYPE_NEXUS_PASSWORD=<nexus password>
   ```
1. Configure git with your codesigning key; make sure it's the same as the one
   you use to sign binaries (i.e. it's the same one you added to gradle.properties):
   ```bash
   # Do this for the repo only
   git config user.email "your@email.com"
   git config user.signingKey "your-key-id"
   ```
1. Add your plugins.gradle.org credentials to ~/.gradle/gradle.properties:
   ```gradle
   gradle.publish.key=<the key>
   gradle.publish.secret=<the secret>
   ```

########################################################################
### Pushing a SNAPSHOT build to local repository
########################################################################

Publish to local repository
```bash
./gradlew publishToMavenLocal
```

Remote local repository to check remote build
```bash
rm -rv ~/.m2/repository/ru/cian/huawei-publish-gradle-plugin/<SNAPSHOT_VERSION>
```

########################################################################
### Pushing a SNAPSHOT build to Sonatype
########################################################################
1. Open the plugin directory:
    ```
    cd ./plugin
    ```
1. Edit gradle.properties, add '-SNAPSHOT' to the VERSION property
1. Upload binaries to Sonatype:
   ```bash
   ./gradlew publishHuaweiPublicationToMavenRepository
   ```
1. Check snapshot: https://oss.sonatype.org/#nexus-search;quick~ru.cian

########################################################################
### Pushing a release build to Sonatype
########################################################################
1. Open the plugin directory:
    ```
    cd ./plugin
    ```
1. Edit gradle.properties, remove '-SNAPSHOT' from the VERSION property
1. Edit readme so that Gradle examples point to the new version
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
   ./gradlew publishHuaweiPublicationToMavenRepository
   ```
1. Check uploaded files and version Sonatype site: [search.maven.org](https://search.maven.org/search?q=ru.cian) 
   and [repo1.maven.org](https://repo1.maven.org/maven2/ru/cian/huawei-publish-gradle-plugin/)   
1. Go to [oss.sonatype.org](https://oss.sonatype.org), log in with your credentials
1. Click "Staging Repositories"
1. Find the "ru.cian" repo, usually at the bottom of the list
1. "Close" the repository (select it then click the "close" button up top), the text field doesn't matter so put whatever you want in it
1. Wait until that's done
1. "Release" the repository, leave the checkbox "Automatically Drop" checked. Yeap, we're in Maven Central now!

########################################################################
### Pushing a release build to Bintray
########################################################################
1. Open the plugin directory:
    ```
    cd ./plugin
    ```
1. Edit gradle.properties, remove '-SNAPSHOT' from the VERSION property
1. Upload binaries to Bintray:
   ```bash
   ./gradlew build bintrayUpload
   ```
1. Check uploaded files and version Bintray site: [bintray.com](https://bintray.com/myumatov/ru.cian/huawei-publish-gradle-plugin)   

########################################################################
### Pushing a release build to Gradle Plugin Portal
########################################################################
1. Open the plugin directory:
    ```
    cd ./plugin
    ```
1. Edit gradle.properties, remove '-SNAPSHOT' from the VERSION property
1. Upload binaries to Gradle's plugin portal:
   ```bash
   ./gradlew publishPlugins
   ```
1. Check uploaded files and version Gradle Plugin Portal site: https://plugins.gradle.org/

########################################################################
### Prepare Release Commit
########################################################################
1. Edit gradle.properties, remove '-SNAPSHOT' from the VERSION property
1. Make a *signed* commit:
   ```bash
   git commit -m "Release vX.Y.Z"
   ```
1. Make a *signed* tag (check existing tags for message format):
   ```bash
   git tag -s -a "vX.Y.Z"
   ```
1. Push all of our work to Github to make it official:
   ```bash
   git push --tags origin master

########################################################################
### Prepare Next Snapshot Version Commit
########################################################################
1. Edit gradle.properties, bump the version number and add '-SNAPSHOT'
1. Make a *signed* commit:
   ```bash
   git commit -m "Prepare next development version"
   ```
