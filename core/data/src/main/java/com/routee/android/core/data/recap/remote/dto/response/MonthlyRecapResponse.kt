package com.routee.android.core.data.recap.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class MonthlyRecapResponse(
    @SerialName("year")
    val year: Int,
    @SerialName("month")
    val month: Int,
    @SerialName("activities")
    val activities: List<ActivityRecapResponse>,
)

@Serializable
internal data class ActivityRecapResponse(
    @SerialName("activityId")
    val activityId: Long,
    @SerialName("title")
    val title: String,
    @SerialName("activityDate")
    val activityDate: String,
    @SerialName("timelineImageUrls")
    val timelineImageUrls: List<String> = emptyList(),
)
