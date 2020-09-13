# Huawei App Gallery Publishing

[![Maven Central](https://img.shields.io/maven-central/v/ru.cian/huawei-publish-gradle-plugin.svg)](https://search.maven.org/search?q=a:huawei-publish-gradle-plugin)
![Version](https://img.shields.io/badge/Version-1.0.1-green.svg)
![Version](https://img.shields.io/badge/Version-1.0.1_snapshot-yellow.svg)
[![License](https://img.shields.io/github/license/srs/gradle-node-plugin.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

The plugin allows you to publish the Release APK file to the Huawei AppGallery.

For publication the plugin used [Huawei Publish API (v2)](https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-References/agcapi-appid-list_v2)

Support `Gradle v4.1+` 

# Versions

```
LAST_RELEASE_VERSION = 1.0.1
```
```
LAST_SNAPSHOT_VERSION = 1.0.1-SNAPSHOT
```
# Adding the plugin to your project

in `./app/build.gradle`

```
apply plugin: 'com.android.application'
apply plugin: 'ru.cian.huawei-publish'

huaweiPublish {
    instances {
        release {
            credentialsPath = "$rootDir/huawei-credentials-release.json"
            publish = true
        }
        debug {
            credentialsPath = "$rootDir/huawei-credentials-debug.json"
            publish = true
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
            publish = true
        }
        fullRelease {
            credentialsPath = "$rootDir/huawei-credentials-full-release.json"
            publish = true
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

#### Params

| param           | priority | type    | default value | description                                                                                            |
|-----------------|----------|---------|---------------|--------------------------------------------------------------------------------------------------------|
| credentialsPath | required | string  | null          | File path with AppGallery credentials params (client_id and client_key)                                |
| publish         | optional | boolean | true          | true - upload build file and publish it on all users, <br>false - upload build file without publishing |

#### For Release Plugin version
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
#### For Snapshot Plugin version
```
buildscript {
    repositories {
        maven { url 'https://oss.sonatype.org/content/repositories/snapshots' }
    }

    dependencies {
        classpath "ru.cian:huawei-publish-gradle-plugin:<LAST_SNAPSHOT_VERSION>"
    }
}
```

# Features

The following features are available:

* Publish APK in Huawei AppGallery and auto submit it on all users after getting store approve
* Different settings for different configurations build types and flavors

The following features are missing:

* Upload Release Build without submit on users after Huawei review
* Publish Release Build on a part of users (Huawei Store doesn't support)
* Publish AppBundle file (Huawei Store doesn't support)
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

**Note!** Before uploading APK file you should build it. Be careful. Don't publish old build. 
 
```
./gradlew assembleRelease publishHuaweiAppGalleryRelease
```

You can override each plugin extension parameter dynamically by using project properties. Just add `-Phgp_` to any parameter name. 

For example:
```
./gradlew assembleRelease publishHuaweiAppGalleryRelease \
        -Phgp_publish=false \ 
        -Phgp_credentialsPath="./credencials/huawei-keys.json"
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