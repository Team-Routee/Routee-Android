package com.routee.android.core.map.model

import androidx.compose.runtime.Immutable

/**
 * 카메라 위치
 *
 * @property target 화면 중심 좌표
 * @property zoom   줌 레벨. 클수록 확대 (16 이면 동네 길이 보이는 정도)
 */
@Immutable
data class MapCamera(
    val target: MapPoint,
    val zoom: Double,
)
