package ru.cian.huawei.publish.models.request

import com.google.gson.annotations.SerializedName

internal data class PhasedReleaseRequest(
    @SerializedName("phasedReleaseStartTime")
    val phasedReleaseStartTime: String,
    @SerializedName("phasedReleaseEndTime")
    val phasedReleaseEndTime: String,
    @SerializedName("phasedReleasePercent")
    val phasedReleasePercent: String,
    @SerializedName("phasedReleaseDescription")
    val phasedReleaseDescription: String
)
