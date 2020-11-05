# 1.2.0

##### Add
* Support of release phases

##### Changes
* Rename credentials parameter: `client_key` -> `client_secret`

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

