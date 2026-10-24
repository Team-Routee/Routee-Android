package com.routee.android.core.data.archive.remote.dto.response

import kotlinx.serialization.Serializable

@Serializable
internal data class MonthlyActivitySummaryResponse(
    val result: List<DailyActivitySummaryResponse>,
    val year: Int,
    val month: Int,
)

@Serializable
internal data class DailyActivitySummaryResponse(
    val activityDate: String,
    val totalDurationMinutes: Int,
    val activityCount: Int,
    val coverImageUrl: String? = null,
)
