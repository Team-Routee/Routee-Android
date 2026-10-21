package com.routee.android.core.data.recap.model

import java.time.LocalDate

/**
 * 기록 편집 목록의 기록 1개
 *
 * @param activityId        활동 식별자
 * @param title             기록 제목
 * @param activityDate      운동한 날짜
 * @param timelineImageUrls 타임라인 사진 URL. 촬영 순서대로
 */
data class ActivityRecap(
    val activityId: Long,
    val title: String,
    val activityDate: LocalDate,
    val timelineImageUrls: List<String>,
)
