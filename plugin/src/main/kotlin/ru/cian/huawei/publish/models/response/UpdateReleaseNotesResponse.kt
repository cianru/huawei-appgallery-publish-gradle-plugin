package ru.cian.huawei.publish.models.response

import com.google.gson.annotations.SerializedName

internal data class UpdateReleaseNotesResponse(
    @SerializedName("ret")
    var ret: Ret
)
