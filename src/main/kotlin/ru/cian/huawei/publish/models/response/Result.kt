package ru.cian.huawei.publish.models.response

import com.google.gson.annotations.SerializedName

internal data class Result(
    @SerializedName("resultCode")
    var resultCode: Int,
    @SerializedName("UploadFileRsp")
    var uploadFileRsp: UploadFileRsp?
)
