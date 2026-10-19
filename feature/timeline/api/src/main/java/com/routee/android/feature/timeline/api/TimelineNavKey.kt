package com.routee.android.feature.timeline.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** 운동 종료 결과로 타임라인 화면을 여는 키 */
@Serializable
data class TimelineNavKey(
    val activityId: Long,
    val endedAtEpochMillis: Long,
    val distanceMeters: Double,
    val durationSeconds: Long,
    val maxElevationMeters: Int,
) : NavKey
