# Huawei App Gallery Publishing

[![License](https://img.shields.io/github/license/srs/gradle-node-plugin.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
![Version](https://img.shields.io/badge/Version-0.1.0-green.svg)
![Version](https://img.shields.io/badge/Version-0.1.1_snapshot-yellow.svg)

The plugin allows you to publish the Release APK file to the Huawei AppGallery.

For publication the plugin used [Huawei Publish API (v2)](https://developer.huawei.com/consumer/en/service/hms/catalog/AGCConnectAPI.html)

Support `Gradle v4.1+` 

# Versions

```
LAST_RELEASE_VERSION = 0.1.0
```
```
LAST_SNAPSHOT_VERSION = 0.1.1-SNAPSHOT
```
# Adding the plugin to your project

in `./app/build.gradle`

```
apply plugin: 'com.android.application'
apply plugin: 'huawei-publish'

huaweiPublish {
    credentialsPath = "<YOUR_PATH>/huawei-credentials.json"
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

#### For Release version 
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
#### For Snapshot version 
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

* Publish APK in Huawei AppGallery and submit it on all users after getting store approve 

The following features are missing:

* Support build flavors
* Upload Release Build without publishing on users 
* Publish Release Build on a part of users
* Publish AppBundle file (Huawei Store doesn't support AppBundle)
* Change App Store Information: description, app icon, screenshots and etc

# Usage 

Gradle generate `publishHuaweiAppGallery*` task for each `buildType` those have `debuggable=false` option.

**Note!** Before uploading APK file you should build it. Be careful. Don't publish old build. 
 
```
./gradlew assembleRelease publishHuaweiAppGalleryRelease
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