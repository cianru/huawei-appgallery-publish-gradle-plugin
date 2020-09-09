package ru.cian.huawei.publish.models.request

import com.google.gson.annotations.SerializedName

internal data class UpdateAppFileInfoRequest(
    @SerializedName("fileType")
    var fileType: Int,
    @SerializedName("files")
    var files: List<FileInfoRequest>
)
