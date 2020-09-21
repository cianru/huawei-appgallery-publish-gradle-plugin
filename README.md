# Huawei App Gallery Publishing

[![Maven Central](https://img.shields.io/maven-central/v/ru.cian/huawei-publish-gradle-plugin.svg)](https://search.maven.org/search?q=a:huawei-publish-gradle-plugin)
![Version](https://img.shields.io/badge/Version-1.1.0-green.svg)
![Version](https://img.shields.io/badge/Version-1.1.1_snapshot-yellow.svg)
![Version](https://img.shields.io/badge/Gradle-4.1+_snapshot-pink.svg)
[![License](https://img.shields.io/github/license/srs/gradle-node-plugin.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

The plugin allows you to publish the android release build file (*.apk or *.aab) to the Huawei AppGallery.

For publication the plugin used [Huawei Publish API (v2)](https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-References/agcapi-appid-list_v2)

# Adding the plugin to your project

in `./app/build.gradle`

```
buildscript {
    repositories {
        mavenCentral() // or jcenter()
    }

    dependencies {
        classpath "ru.cian:huawei-publish-gradle-plugin:<LAST_RELEASE_VERSION>"
    }
}
```
<details>
<summary>Snapshot builds are also available</summary>

<p>
You'll need to add the Sonatype snapshots repository:

```kotlin
buildscript {
    repositories {
        maven { url 'https://oss.sonatype.org/content/repositories/snapshots' }
    }

    dependencies {
        classpath "ru.cian:huawei-publish-gradle-plugin:<LAST_SNAPSHOT_VERSION>"
    }
}
```
</p>
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
  "client_key": "<CLIENT_KEY>"
}
```
Credentials you should get at Huawei AppGallery Developer Console.  

#### Plugin params

| param           | priority | type    | default value | cli                       | description                                                                                            |
|-----------------|----------|---------|---------------|---------------------------|--------------------------------------------------------------------------------------------------------|
| credentialsPath | required | string  | null          | --credentialsPath         | File path with AppGallery credentials params (client_id and client_key)                                |
| publish         | optional | boolean | true          | --publish<br>--no-publish | true - upload build file and publish it on all users, <br>false - upload build file without publishing |
| buildFormat     | optional | string  | "apk"         | --buildFormat             | "apk" or "aab" for corresponding build format                                                          |
| buildFile       | optional | string  | null          | --buildFile               | Path to build file. "null" means use standard path for "apk" and "aab" files.                          | 

# Features

The following features are available:

* Publish APK in Huawei AppGallery and auto submit it on all users after getting store approve
* Different settings for different configurations build types and flavors

The following features are missing:

* Publish Release Build on a part of users (Huawei Store doesn't support)
* Change App Store Information: description, app icon, screenshots and etc.

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
    --no-publish \ 
    --credentialsPath="/sample1/huawei-credentials.json" \
    --buildFormat="apk"
```

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
