package com.routee.android.core.map

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.core.map.model.MapPhoto
import com.routee.android.core.map.model.MapPoint

/** 경로와 촬영 위치의 사진 썸네일을 표시하는 지도 */
@Composable
fun RouteePhotoRouteMap(
    route: List<MapPoint>,
    photos: List<MapPhoto>,
    modifier: Modifier = Modifier,
) {
    val isPreview = LocalInspectionMode.current
    val radiusDp = RouteeTheme.radius.Radius12
    val cameraPadding = if (photos.isEmpty()) {
        RouteeTheme.spacing.Spacing20
    } else {
        PhotoMarkerSize +
            RouteeTheme.spacing.Spacing12
    }
    val cameraPoints = remember(route, photos) {
        route + photos.map { it.point }
    }
    val state = rememberRouteeMapState()
    var mapSize by remember { mutableStateOf(IntSize.Zero) }
    val markers = rememberPhotoMarkers(photos)

    LaunchedEffect(state, cameraPoints, mapSize, cameraPadding, isPreview) {
        if (!isPreview && mapSize.width > 0 && mapSize.height > 0) {
            state.fitBounds(cameraPoints, padding = cameraPadding)
        }
    }
    RouteeNaverMap(
        state = state,
        route = route,
        markers = markers,
        modifier = modifier
            .fillMaxWidth()
            .height(480.dp)
            .clip(RoundedCornerShape(radiusDp))
            .onSizeChanged { mapSize = it },
    )
}
