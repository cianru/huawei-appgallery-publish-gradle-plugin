package ru.cian.huawei.publish.models.response

import com.google.gson.annotations.SerializedName

internal data class UpdateAppBasicInfoResponse(
    @SerializedName("ret")
    var ret: Ret
)
