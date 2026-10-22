package com.routee.android.core.data.recap.model

/**
 * 기록 편집 화면의 운동 수치와 지도, 루트
 *
 * @param distanceMeters     운동 거리 (m)
 * @param durationSeconds    운동 시간 (초)
 * @param maxElevationMeters 최고 고도 (m)
 * @param mapImageUrl        기본 배경 지도 이미지. 없으면 null
 * @param routes             지나간 루트
 */
data class RecapResource(
    val distanceMeters: Int,
    val durationSeconds: Int,
    val maxElevationMeters: Int,
    val mapImageUrl: String?,
    val routes: List<RecapRoute>,
)

/**
 * @param sequence 지나간 순서
 * @param name     루트 이름
 */
data class RecapRoute(
    val sequence: Int,
    val name: String,
)
