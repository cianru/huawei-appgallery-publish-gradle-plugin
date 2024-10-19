package ru.cian.huawei.publish.models.request

import com.google.gson.annotations.SerializedName

internal data class FileInfoRequest(
    @SerializedName("fileName")
    var fileName: String,
    @SerializedName("fileDestUrl")
    var fileDestUrl: String,
    @SerializedName("size")
    var size: Long
)
