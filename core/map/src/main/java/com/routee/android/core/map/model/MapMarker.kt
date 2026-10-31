package com.routee.android.core.map.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.Dp

/**
 * 지도 마커
 *
 * @property id    탭 시 onMarkerClick 으로 전달
 * @property icon  마커 이미지, 가운데가 좌표에 옴. null 이면 SDK 기본 마커
 * @property size  아이콘 크기. null 이면 이미지 원래 크기
 */
@Immutable
data class MapMarker(
    val id: String,
    val point: MapPoint,
    val icon: ImageBitmap? = null,
    val size: Dp? = null,
)
