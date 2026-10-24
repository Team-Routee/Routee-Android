package com.routee.android.feature.timeline.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.component.skeleton.skeleton
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.core.map.RouteePhotoRouteMap
import com.routee.android.core.ui.UiState
import com.routee.android.feature.timeline.main.model.TimelineTrack

private val MapHeight = 480.dp

@Composable
internal fun TimelineRouteMap(
    track: UiState<TimelineTrack>,
    modifier: Modifier = Modifier,
) {
    if (track is UiState.Success) {
        RouteePhotoRouteMap(route = track.data.route, photos = track.data.photos, modifier = modifier)
    } else {
        // TODO P1 기획: 경로 조회 실패 시 지도 자리 (지금은 토스트 후 멈춘 스켈레톤, 재시도 없음)
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(MapHeight)
                .skeleton(
                    shape = RoundedCornerShape(RouteeTheme.radius.Radius12),
                    animated = track is UiState.Loading,
                ),
        )
    }
}
