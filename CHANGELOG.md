# 1.5.0

##### Add
* [issue#58] Add new `publishSocketTimeoutInSeconds` parameter to change the socket timeout for publish requests in seconds.
* [issue#59] Add new `credentials` parameter to provide credentials as a base64 encoded string.

##### Breaking Changes
* Remove support of Sonatype. It means that you can't use the plugin from Maven Central. You must to use the Gradle Portal.
  To do this, you need to add the following code to your `settings.gradle.kts`:
  ```kotlin
  pluginManagement {
      repositories {
          gradlePluginPortal()
      }
  }
  ```
* Change classpath dependency from `ru.cian:huawei-publish-gradle-plugin:<VERSION>` to `ru.cian.huawei-plugin:plugin:<VERSION>`.
* Remove support of `clientId` and `clientSecret` CLI params. Use `credentials` or `credentialsPath` params instead.

# 1.4.2

##### Add
* [issue#50] Added new parameter `removeHtmlTags` to remove html tags from Release Notes and support of the same Release Notes as for Google Play.

##### Fix
* Fix correct mustRunAfter publish task for `assemble*` and `bundle*` tasks for Gradle 8.

##### Breaking Changes

Changed `releaseNotes` configuration block. Instead of
```
   releaseNotes = listOf(
        ru.cian.rustore.publish.ReleaseNote(
                  lang = "en-US",
                  filePath = "$projectDir/release-notes-en.txt"
        ),
    )
```            
new configuration would
```
    releaseNotes = ru.cian.huawei.publish.ReleaseNotesExtension(
        descriptions = listOf(
            ru.cian.huawei.publish.ReleaseNote(
                    lang = "en-US",
                    filePath = "$projectDir/release-notes-en.txt"
            )
        ),
        removeHtmlTags = false
    )
```

# 1.3.7

##### Fix
* Fix correct mustRunAfter publish task for `assemble*` and `bundle*` tasks for Gradle 7.  

# 1.4.0

##### Add
* Support of [Android Gradle Plugin 8+ and JDK 17+](https://developer.android.com/build/releases/gradle-plugin#jdk-17-agp)

##### Other
* Upgrade Gradle upto 8.1
* Upgrade Android Gradle Plugin upto 8.0.0

# 1.3.6

##### Fix
* [issue#46](https://github.com/cianru/huawei-publish-gradle-plugin/issues/46) Broken Task Configuration Avoidance. Thanks @rmarma for contribution.

# 1.3.5

##### Add
* [issue#41](https://github.com/cianru/huawei-publish-gradle-plugin/issues/21) Add param to update App Basic Info into Developer Console.
  See `appBasicInfo` Gradle Plugin Extension param and `--appBasicInfo` CLI param description.

##### Other
* Upgrade Kolint upto 1.8.0
* Upgrade Gradle upto 7.6
* Upgrade Android Gradle Plugin upto 7.3.1
* Support of [Gradle Version Catalog for sharing dependencies](https://docs.gradle.org/current/userguide/platforms.html)  

# 1.3.4

##### Add
* [issue#37](https://github.com/cianru/huawei-publish-gradle-plugin/issues/37) Update Release Notes for publishing build. 
See `releaseNotes` Gradle Plugin Extension param and `--releaseNotes` CLI param description.

# 1.2.6

##### Fix for supporting of Gradle 6.* and AGP 4.*
* [issue#39](https://github.com/cianru/huawei-publish-gradle-plugin/issues/39):
  Java 8 incompatibility
* CheryPick from v1.3.* [issue#38](https://github.com/cianru/huawei-publish-gradle-plugin/issues/38):
  Publishing bug: The package is being compiled, please try again

# 1.3.3

##### Fix
* [issue#36](https://github.com/cianru/huawei-publish-gradle-plugin/issues/36):
  publishHuaweiAppGallery* task not created for all build types. 
  The plugin create publishing task for debuggable build too.
* [issue#38](https://github.com/cianru/huawei-publish-gradle-plugin/issues/38):
  Publishing bug: "The package is being compiled, please try again".
  Huawei changed the behavior of current api url for apk build copied it as for aab with waiting for review.
  
# 1.3.1

##### Add
* Support of Detekt.
* Execute GitHub actions to run detekt checks on each push and pull request.

##### Fix
* [issue#32](https://github.com/cianru/huawei-publish-gradle-plugin/issues/32):
  Fix correct finding of aab and apk build files.
* [issue#33](https://github.com/cianru/huawei-publish-gradle-plugin/issues/33):
  Publish task must run after assemble and bundle tasks.

# 1.3.0

##### Add
* [issue#25](https://github.com/cianru/huawei-publish-gradle-plugin/issues/25):
Gradle 7.0 / Android Gradle Plugin 7.0 support
* add unit tests to automated github actions

# 1.2.4

##### Add
* Support of Gradle Portal and Gradle DSL..

##### Breaking changes
* Removed `clientId` and `clientSecret` plugin extension params as unsecured way for them setting.

##### Fix
* [issue#21](https://github.com/cianru/huawei-publish-gradle-plugin/issues/21):
App publication requires additional manual step to be available for users

# 1.2.2

##### Breaking changes
* If you are using AppBundle the current plugin version supports only Android Gradle Plugin v4.1.* due to
[AGP Issue](https://issuetracker.google.com/issues/109918868/). If you are using APK, then AGP version and Plugin version are irrelevant.

##### Fix
* [issue#11](https://github.com/cianru/huawei-publish-gradle-plugin/issues11): Handle Api Error for wrong `client_id` or `client_secret` values;
* [issue#16](https://github.com/cianru/huawei-publish-gradle-plugin/issues16): Plugin incorrectly detect AppBundle file location for AGP v4.1.*;

# 1.2.1

##### Add
* Support of release phases

##### Breaking changes
* Rename credentials parameter: `client_key` -> `client_secret`
* Replace `publish` plugin option on `deployType` one

##### Fix
* [issue#7](https://github.com/cianru/huawei-publish-gradle-plugin/issues/7):
AppBundle publication error: The file is being processed. It may take 2-5 minutes, depending on the size of the software package
* [issue#10](https://github.com/cianru/huawei-publish-gradle-plugin/issues/10):
Get error "call cds to query app information failed" for publishing with release phase

# 1.1.0

##### Add
* Support AppBundle
* Support system proxy
* Add CLI params for dynamically changes the plugin extension params

##### Fix
* [Issue#2](https://github.com/cianru/huawei-publish-gradle-plugin/issues/2):
Error on upload: 'The language does not exist!'

# 1.0.1

* Update dependencies: kotlin-1.3.72, gradle-6.6
* Added `publish` param to separate uploading and publishing a build file

# 1.0.0

* Support different Plugin settings for different buildTypes and flavors
* First release version

# 0.1.0

First released version. Support:
* Publish APK in Huawei AppGallery and submit it on all users after getting store approve

