package ru.cian.huawei.publish.models.response

import com.google.gson.annotations.SerializedName

internal data class UploadFileRsp(
    /**
     * 0: failed
     * 1: successful
     */
    @SerializedName("ifSuccess")
    val ifSuccess: Int,
    @SerializedName("fileInfoList")
    val fileInfoList: List<FileInfo>
)
