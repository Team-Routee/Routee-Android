package com.routee.android.feature.recordedit.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

/**
 * 기록 편집 메인의 기록 카드 1개
 *
 * @param activityId 활동 식별자
 * @param title 기록 제목 (서버 기본값: "YYYY.MM.DD 기록")
 * @param workoutDate 화면 표시용 날짜 (예: "2026.03.23.")
 * @param thumbnailUrls 촬영 순서대로 정렬된 타임라인 사진 URL (최대 4장)
 */
@Immutable
internal data class RecapItemUiModel(
    val activityId: Long,
    val title: String,
    val workoutDate: String,
    val thumbnailUrls: ImmutableList<String>,
)
