package ru.cian.huawei.publish.models.request

import com.google.gson.annotations.SerializedName

internal data class UpdateReleaseNotesRequest(
    /**
     * String(64)
     * Language. For details, please refer to Languages.
     */
    @SerializedName("lang")
    var lang: String,
    /**
     * String(500)
     * New features in a language.
     */
    @SerializedName("newFeatures")
    var newFeatures: String
)
