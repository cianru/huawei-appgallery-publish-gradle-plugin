# Huawei App Gallery Publishing

[![Maven Central](https://img.shields.io/maven-central/v/ru.cian/huawei-publish-gradle-plugin.svg)](https://search.maven.org/search?q=a:huawei-publish-gradle-plugin)
![Version](https://img.shields.io/badge/Version-1.2.0-green.svg)
![Version](https://img.shields.io/badge/Gradle-4.1+-pink.svg)
[![License](https://img.shields.io/github/license/srs/gradle-node-plugin.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

The plugin allows you to publish the android release build file (*.apk or *.aab) to the Huawei AppGallery.

For publication the plugin used [Huawei Publish API (v2)](https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-References/agcapi-appid-list_v2)

# Features

The following features are available:

* Publish APK or AAB build file in Huawei AppGallery
* Submit the build on all users after getting store approve
* Publish the build on a part of users (Release Phases)
* Separated settings for different configurations build types and flavors

The following features are missing:

* Support of Gradle Portal and Gradle DSL.
* Change App Store Information: description, app icon, screenshots and etc.
* Add Release Notes for publishing build.

# Adding the plugin to your project

in application module `./app/build.gradle`

```
buildscript {
    repositories {
        mavenCentral() // or jcenter()
    }

    dependencies {
        classpath "ru.cian:huawei-publish-gradle-plugin:<VERSION>"
    }
}
```
<details>
<summary>Snapshot builds are also available</summary>
___
You'll need to add the Sonatype snapshots repository.
Look for the actual version of the snapshot in the name of the opened `snapshot-*` repository branch.

```kotlin
buildscript {
    repositories {
        maven { url 'https://oss.sonatype.org/content/repositories/snapshots' }
    }

    dependencies {
        classpath "ru.cian:huawei-publish-gradle-plugin:<SNAPSHOT_VERSION>"
    }
}
```
___
</details>

```
apply plugin: 'com.android.application'
apply plugin: 'ru.cian.huawei-publish'

huaweiPublish {
    instances {
        release {
            credentialsPath = "$rootDir/huawei-credentials-release.json"
            buildFormat = "apk"
            ...
        }
        debug {
            credentialsPath = "$rootDir/huawei-credentials-debug.json"
            buildFormat = "apk"
            ...
        }
    }
}
```

Plugin supports different settings for different buildType and flavors.
For example, for `demo` and `full` flavors and `release` buildType just change instances like that:
```
huaweiPublish {
    instances {
        demoRelease {
            credentialsPath = "$rootDir/huawei-credentials-demo-release.json"
            buildFormat = "apk"
            ...
        }
        fullRelease {
            credentialsPath = "$rootDir/huawei-credentials-full-release.json"
            buildFormat = "apk"
            ...
        }
    }
}
```

File `huawei-credentials.json` contains next json structure:
```
{
  "client_id": "<CLIENT_ID>",
  "client_secret": "<CLIENT_SECRET>"
}
```
How to get credentials see [AppGallery Connect API Getting Started](https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-Guides/agcapi-getstarted).

# Plugin params
Where Priority(P), Required(R), Optional(O)

| param            | P | type       | default value | cli                    | description                                                                                                 |
|------------------|---|------------|---------------|------------------------|-------------------------------------------------------------------------------------------------------------|
| credentialsPath  | O | string     | null          | --credentialsPath      | File path with AppGallery credentials params (`client_id` and `client_secret`)                              |
| clientId         | O | string     | null          | --clientId             | `client_id` param from AppGallery credentials. The key more priority than value from `credentialsPath`      |
| clientSecret     | O | string     | null          | --clientSecret         | `client_secret` param from AppGallery credentials. The key more priority than value from `credentialsPath`  |
| deployType       | O | string     | "publish"     | --deployType           | '`publish`' to deploy and submit build on users,<br>'`draft`' to deploy and save as draft without submit on users,<br>'`upload-only`' to deploy without draft saving and submit on users|
| publishTimeoutMs | O | long       | 600000 #(10m) | --publishTimeoutMs     | The time in millis during which the plugin periodically tries to publish the build                          |
| publishPeriodMs  | O | long       | 15000 #(15s)  | --publishPeriodMs      | The period in millis between tries to publish the build                                                     |
| buildFormat      | O | string     | "apk"         | --buildFormat          | 'apk' or 'aab' for corresponding build format                                                               |
| buildFile        | O | string     | null          | --buildFile            | Path to build file. "null" means use standard path for "apk" and "aab" files.                               |
| releaseTime      | O | string     | null          | --releaseTime          | Release time after review in UTC format. The format is 'yyyy-MM-dd'T'HH:mm:ssZZ'.                           |
| releasePhase     | O | Phase      | null          | (see Phase param desc.)| Release Phase. For mote info see documentation below.                                                       |

other params

| Phase            | P | type    | default value | cli                       | description                                                                                                |
|------------------|---|---------|---------------|---------------------------|------------------------------------------------------------------------------------------------------------|
| startTime        | R | string  | null          | --releasePhaseStartTime   | Start release time after review in UTC format. The format is 'yyyy-MM-dd'T'HH:mm:ssZZ'.                    |
| endTime          | R | string  | null          | --releasePhaseEndTime     | End release time after review in UTC format. The format is 'yyyy-MM-dd'T'HH:mm:ssZZ'.                      |
| percent          | R | string  | null          | --releasePhasePercent     | Percentage of target users of release by phase. The integer or decimal value from 0 to 100.                |

# Usage

Gradle generate `publishHuaweiAppGallery*` task for each buildType configuration those have `debuggable=false` option.
```
android {
    buildTypes {
        release {
            debuggable false
            ...
        }
    }
}
```

**Note!** Before uploading build file you should build it. Be careful. Don't publish old build file. 
 
```
./gradlew assembleRelease publishHuaweiAppGalleryRelease
```

or 

```
./gradlew bundleRelease publishHuaweiAppGalleryRelease
```

You can override each plugin extension parameter dynamically by using CLI params. For example:

```
./gradlew assembleRelease publishHuaweiAppGalleryRelease \
    --deployType=publish \
    --credentialsPath="/sample1/huawei-credentials.json" \
    --buildFormat=apk
```

<details>
<summary>Example  uploading build file without publishing</summary>

You can upload the build file as draft without submit on users.

From gradle build script:
```
huaweiPublish {
    instances {
        release {
            credentialsPath = "$rootDir/sample1/huawei-credentials.json"
            deployType = "draft"
        }
    }
}
```

or execute from command line:

```
./gradlew assembleRelease publishHuaweiAppGalleryRelease \
    --credentialsPath="$rootDir/sample1/huawei-credentials.json" \
    --deployType=draft
```

</details>

<details>
<summary>Example publishing AppBundle</summary>

If you choose AppBundle see [Application Signature](https://developer.huawei.com/consumer/en/service/josp/agc/index.html#/myApp/101338815/9249519184596012000) before using the plugin.

From gradle build script:
```
huaweiPublish {
    instances {
        release {
            credentialsPath = "$rootDir/sample1/huawei-credentials.json"
            buildFormat = "aab"
        }
    }
}
```
or execute from command line:

```
./gradlew assembleRelease publishHuaweiAppGalleryRelease \
    --credentialsPath="$rootDir/sample1/huawei-credentials.json" \
    --buildFormat=aab
```

After uploading *.aab file the Huawei Service will start processed. It may take 2-5 minutes, depending on the size of the software package.
While publishing the AppBundle build file you can get the error:
>What went wrong:
> Execution failed for task ':app:publishHuaweiAppGalleryRelease'.
> Ret(msg=[cds]submit failed, additional msg is [The file is being processed.
> It may take 2-5 minutes, depending on the size of the software package.])

When publishing an AppBundle the service takes some time to parse the file.
In this case, the plugin uses a special mechanism for the full cycle.
By default, the plugin tries to publish the assembly every 15 seconds for 10 minutes.
To change values see using parameters: `publishTimeoutMs` and `publishPeriodMs`.
You don't meet such problem for *.apk file which will publish immediately after uploading.

For more information see the [Issue#7](https://github.com/cianru/huawei-publish-gradle-plugin/issues/7)

</details>

<details>
<summary>Example publishing with release phase</summary>

You can upload the build file and submit it on the part of users.

From gradle build script:
```
huaweiPublish {
    instances {
        release {
            credentialsPath = "$rootDir/sample1/huawei-credentials.json"
            releasePhase {
                startTime = "2020-11-13T08:01:02+0300"
                endTime = "2020-11-20T15:30:00+0300"
                percent = 10.0
            }
        }
    }
}
```

or execute from command line:

```
./gradlew assembleRelease publishHuaweiAppGalleryRelease \
    --clientId=<CLIENT_ID> \
    --clientSecret=<CLIENT_SECRET> \
    --releasePhaseStartTime=2020-11-13T08:01:02+0300 \
    --releasePhaseEndTime=2020-11-20T15:30:00+0300 \
    --releasePhasePercent=10.0
```

While publishing with release phase you can get the error:
>Execution failed for task ':app:publishHuaweiAppGalleryRelease'.
>Update App File Info is failed. Response: UpdateAppFileInfoResponse(ret=Ret(code=204144644, msg=[AppGalleryConnectPublishService]call cds to query app information failed))

I asked Huawei support. They confirmed the server issue. To work around this problem you should once set
the release phase for uploader build from Developer Console. After that plugin should publish next builds without this error.
(Sorry for RU screenshot interface locale. Huawei doesn't allow me to change it on EN)

![screenshot](screenshots/huawei-release-phase-isssue-1.png)

One more note. If already there is published version that waiting for review you'll get error:

>What went wrong:
>Execution failed for task ':app:publishHuaweiAppGalleryRelease'.
>Update App File Info is failed. Response: UpdateAppFileInfoResponse(ret=Ret(code=204144647, msg=[cds]update service failed, additional msg is [The new service has can't be edited service,which can't be updated!]))

For more information see the [Issue#10](https://github.com/cianru/huawei-publish-gradle-plugin/issues/10)

</details>

# License

```
Copyright 2020 Aleksandr Mirko

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
