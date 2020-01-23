package ru.cian.huawei.publish.models.response

import com.google.gson.annotations.SerializedName

internal data class AppIdResponse(
    @SerializedName("ret")
    var ret: Ret,
    @SerializedName("appids")
    val appids: List<AppInfo>
)
