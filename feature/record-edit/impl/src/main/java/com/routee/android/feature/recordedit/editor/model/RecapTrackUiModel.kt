package com.routee.android.feature.recordedit.editor.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * 사진 타임라인 스티커에 그릴 경로 (GET /api/v1/activity/{activityId}/track)
 *
 * @param trackPoints  운동 경로. 서버 pointIndex 순서
 * @param markers      경로 위 사진. 촬영 순서
 */
@Immutable
internal data class RecapTrackUiModel(
    val trackPoints: ImmutableList<GeoPoint>,
    val markers: ImmutableList<TimelineMarker>,
) {
    @Immutable
    data class GeoPoint(
        val latitude: Double,
        val longitude: Double,
    )

    @Immutable
    data class TimelineMarker(
        val timelineId: Long,
        val thumbnailUrl: String,
        val point: GeoPoint,
    )

    companion object {
        val EMPTY = RecapTrackUiModel(trackPoints = persistentListOf(), markers = persistentListOf())
    }
}
