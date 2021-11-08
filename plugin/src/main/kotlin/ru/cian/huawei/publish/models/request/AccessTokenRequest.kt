package ru.cian.huawei.publish.models.request

import com.google.gson.annotations.SerializedName

internal data class AccessTokenRequest(
    @SerializedName("client_id")
    val clientId: String,
    @SerializedName("client_secret")
    val clientSecret: String,
    @SerializedName("grant_type")
    val grantType: String
)
