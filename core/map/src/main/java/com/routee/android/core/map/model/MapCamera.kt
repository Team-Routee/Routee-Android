package com.routee.android.core.map.model

import androidx.compose.runtime.Immutable

/**
 * 카메라 위치
 *
 * @property target 화면 중심 좌표
 * @property zoom   줌 레벨
 */
@Immutable
data class MapCamera(
    val target: MapPoint,
    val zoom: Double,
)
