package com.routee.android.core.map.model

import androidx.compose.runtime.Immutable

/** 지도 좌표 */
@Immutable
data class MapPoint(
    val latitude: Double,
    val longitude: Double,
)
