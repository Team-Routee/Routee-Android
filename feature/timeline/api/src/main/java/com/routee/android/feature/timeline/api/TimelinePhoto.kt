package com.routee.android.feature.timeline.api

import kotlinx.serialization.Serializable

/** 운동 중 촬영한 사진을 타임라인 화면에 전달하는 데이터 */
@Serializable
data class TimelinePhoto(
    val imageUrl: String,
    val capturedAtEpochMillis: Long,
    val location: String? = null,
)
