package ru.cian.huawei.publish.models.response

import com.google.gson.annotations.SerializedName

internal data class UploadUrlResponse(
    @SerializedName("ret")
    var ret: Ret,
    @SerializedName("uploadUrl")
    val uploadUrl: String,
    @SerializedName("chunkUploadUrl")
    val chunkUploadUrl: String,
    @SerializedName("authCode")
    val authCode: String
)
