package ru.cian.huawei.publish.models

import com.google.gson.annotations.SerializedName

internal data class Credential(
    @SerializedName("client_id")
    val clientId: String?,
    @SerializedName("client_secret")
    val clientSecret: String?
)
