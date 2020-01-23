package ru.cian.huawei.publish.models.response

import com.google.gson.annotations.SerializedName

internal data class Ret(
    @SerializedName("code")
    var code: Int,
    @SerializedName("msg")
    var msg: String
)
