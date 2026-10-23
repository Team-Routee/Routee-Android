package com.routee.android.core.map.model

import androidx.compose.runtime.Immutable

/** 경로 위 사진 마커의 식별자·이미지 주소·촬영 위치 */
@Immutable
data class MapPhoto(
    val id: String,
    val imageUrl: String,
    val point: MapPoint,
)
