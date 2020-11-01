package ru.cian.huawei.publish.models.response

import com.google.gson.annotations.SerializedName

internal data class AccessTokenResponse(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("expires_in")
    val expiresIn: Long
)
