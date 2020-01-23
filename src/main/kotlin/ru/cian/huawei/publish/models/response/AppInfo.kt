package ru.cian.huawei.publish.models.response

import com.google.gson.annotations.SerializedName

internal data class AppInfo(
    @SerializedName("key")
    val key: String,
    @SerializedName("value")
    val value: String
)
