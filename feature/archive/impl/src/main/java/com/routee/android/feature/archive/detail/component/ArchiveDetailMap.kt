package com.routee.android.feature.archive.detail.component

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
import com.routee.android.core.map.RouteeNaverMap
import com.routee.android.core.map.model.MapMarker
import com.routee.android.core.map.model.MapPoint
import com.routee.android.core.map.rememberRouteeMapState

@Composable
internal fun ArchiveDetailMap(
    track: List<MapPoint>,
    markers: List<MapMarker>,
    modifier: Modifier = Modifier,
) {
    val state = rememberRouteeMapState()
    val isPreview = LocalInspectionMode.current
    val cameraPadding = maxOf(
        RouteeTheme.spacing.Spacing20,
        (markers.maxOfOrNull { it.size ?: 42.dp } ?: 0.dp) + RouteeTheme.spacing.Spacing12,
    )
    val cameraPoints = remember(track, markers) { track + markers.map { it.point } }
    var mapSize by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(state, cameraPoints, mapSize, cameraPadding, isPreview) {
        if (!isPreview && mapSize.width > 0 && mapSize.height > 0) {
            state.fitBounds(cameraPoints, padding = cameraPadding)
        }
    }
    RouteeNaverMap(
        state = state,
        route = track,
        markers = markers,
        modifier = modifier
            .fillMaxWidth()
            .height(480.dp)
            .clip(RoundedCornerShape(RouteeTheme.radius.Radius12))
            .onSizeChanged { mapSize = it },
    )
}
