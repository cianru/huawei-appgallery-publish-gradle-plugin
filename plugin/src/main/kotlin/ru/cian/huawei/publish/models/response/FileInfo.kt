package ru.cian.huawei.publish.models.response

import com.google.gson.annotations.SerializedName

internal data class FileInfo(
    @SerializedName("fileDestUlr")
    var fileDestUlr: String,
    @SerializedName("imageResolution")
    var imageResolution: String,
    @SerializedName("imageResolutionSingature")
    var imageResolutionSignature: String,
    @SerializedName("size")
    var size: Long
)
