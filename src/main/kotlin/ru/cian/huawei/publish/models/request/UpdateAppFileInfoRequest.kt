package ru.cian.huawei.publish.models.request

import com.google.gson.annotations.SerializedName

internal data class UpdateAppFileInfoRequest(
    @SerializedName("lang")
    var lang: String,
    @SerializedName("fileType")
    var fileType: Int,
    @SerializedName("files")
    var files: List<FileInfoRequest>
)
