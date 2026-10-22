package com.routee.android.core.data.archive.model

import java.time.LocalDate

/** 일별 운동 합계(분), 기록 수, 대표 사진 */
data class DailyActivitySummary(
    val date: LocalDate,
    val totalDurationMinutes: Int,
    val activityCount: Int,
    val coverImageUrl: String?,
)
