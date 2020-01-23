package ru.cian.huawei.publish.models.response

import com.google.gson.annotations.SerializedName

internal data class FileServerOriResultResponse(
    @SerializedName("result")
    var result: Result
)
