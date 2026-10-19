package com.routee.android.feature.recordedit.component.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.component.RouteeAsyncImage
import com.routee.android.core.designsystem.preview.PreviewFontSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.feature.recordedit.editor.model.RecapTrackUiModel
import com.routee.android.feature.recordedit.model.RecapColor
import com.routee.android.feature.recordedit.model.color
import kotlinx.collections.immutable.toImmutableList

private val MarkerSize = PhotoTimelineStickerLayout.MARKER_SIZE_DP.dp
private val MarkerBorderThickness = 3.dp

/**
 * 사진 타임라인 스티커 (map_photo_route)
 *
 * 운동 경로 선 위에 촬영한 사진을 놓는다. 사진이 없으면 경로 선만 그린다
 *
 * @param layout          [PhotoTimelineStickerLayout.create]로 만든 배치
 * @param thumbnailUrls   사진 주소. [layout]의 사진 좌표와 같은 순서
 * @param color           색상 팔레트에서 선택한 색. 경로 선과 사진 테두리에 적용
 */
@Composable
internal fun PhotoTimelineSticker(
    layout: PhotoTimelineStickerLayout,
    thumbnailUrls: List<String>,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val markerShape = RoundedCornerShape(RouteeTheme.radius.Radius12)

    Box(modifier = modifier.size(layout.size.width.dp, layout.size.height.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawPath(
                path = layout.routePoints.toPath(density = this),
                color = color,
                style = Stroke(
                    width = PhotoTimelineStickerLayout.STROKE_WIDTH.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                ),
            )
        }
        layout.markerCenters.zip(thumbnailUrls).forEach { (center, url) ->
            RouteeAsyncImage(
                url = url,
                contentDescription = null,
                modifier = Modifier
                    .offset(x = center.x.dp - MarkerSize / 2, y = center.y.dp - MarkerSize / 2)
                    .size(MarkerSize)
                    .clip(markerShape)
                    .border(MarkerBorderThickness, color, markerShape),
            )
        }
    }
}

private fun List<Offset>.toPath(density: Density): Path = Path().apply {
    forEachIndexed { index, point ->
        val x = with(density) { point.x.dp.toPx() }
        val y = with(density) { point.y.dp.toPx() }
        if (index == 0) moveTo(x, y) else lineTo(x, y)
    }
}

@PreviewFontSizes
@Composable
private fun PhotoTimelineStickerPreview() {
    val track = RecapTrackUiModel(
        trackPoints = listOf(
            RecapTrackUiModel.GeoPoint(37.600, 126.960),
            RecapTrackUiModel.GeoPoint(37.596, 126.965),
            RecapTrackUiModel.GeoPoint(37.590, 126.962),
            RecapTrackUiModel.GeoPoint(37.585, 126.970),
        ).toImmutableList(),
        markers = listOf(
            RecapTrackUiModel.TimelineMarker(1L, "", RecapTrackUiModel.GeoPoint(37.596, 126.965)),
        ).toImmutableList(),
    )
    val layout = PhotoTimelineStickerLayout.create(track, Size(328f, 583f)) ?: return

    RouteePreviewTheme {
        Box(modifier = Modifier.size(328.dp, 583.dp)) {
            PhotoTimelineSticker(
                layout = layout,
                thumbnailUrls = track.markers.map { it.thumbnailUrl },
                color = RecapColor.DEFAULT.color,
                modifier = Modifier.offset(layout.position.x.dp, layout.position.y.dp),
            )
        }
    }
}
