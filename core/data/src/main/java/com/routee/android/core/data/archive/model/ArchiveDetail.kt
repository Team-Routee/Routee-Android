package com.routee.android.core.data.archive.model

import com.routee.android.core.data.recap.model.ActivityTrack
import com.routee.android.core.data.recap.model.RecapRoute
import java.time.Instant
import java.time.LocalDate

/** 활동 ID로 조회한 운동 수치·경로·사진·방문 지점 */
data class ArchiveDetail(
    val title: String,
    val date: LocalDate,
    val distanceMeters: Int,
    val durationSeconds: Int,
    val maxElevationMeters: Int,
    val track: ActivityTrack,
    val photos: List<ArchivePhoto>,
    val routes: List<RecapRoute>,
)

/** 촬영 시각 순으로 표시할 타임라인 사진 */
data class ArchivePhoto(
    val id: Long,
    val location: String?,
    val imageUrl: String,
    val createdAt: Instant,
)
