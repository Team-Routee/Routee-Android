package com.routee.android.feature.recordedit.component.editor

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import com.routee.android.feature.recordedit.editor.model.RecapTrackUiModel
import kotlin.math.min
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

// map_photo_route
private const val CANVAS_PADDING = 60f
private const val ROUTE_STROKE_WIDTH = 3f
private const val MARKER_SIZE = 42f
private const val STICKER_INSET = 12f
private const val STICKER_BOTTOM_OFFSET = 36f

/**
 * 사진 타임라인 스티커 배치
 *
 * @param size           스티커 크기
 * @param position       캔버스 안 기본 위치 (왼쪽 위)
 * @param routePoints    스티커 안 경로 좌표
 * @param markerCenters  스티커 안 사진 중심 좌표. [RecapTrackUiModel.markers]와 같은 순서
 */
@Immutable
internal data class PhotoTimelineStickerLayout(
    val size: Size,
    val position: Offset,
    val routePoints: ImmutableList<Offset>,
    val markerCenters: ImmutableList<Offset>,
) {
    companion object {
        const val STROKE_WIDTH = ROUTE_STROKE_WIDTH
        const val MARKER_SIZE_DP = MARKER_SIZE

        /**
         * 경로를 캔버스 크기에 맞춰 비율 그대로 가운데 놓고, 스티커는 캔버스 아래쪽에 둔다
         *
         * @return 경로 좌표가 2개 미만이거나 한 점에 몰려 있으면 null (스티커 없음)
         */
        fun create(
            track: RecapTrackUiModel,
            canvasSize: Size,
        ): PhotoTimelineStickerLayout? {
            val projection = TrackProjection.create(track.trackPoints, canvasSize, CANVAS_PADDING) ?: return null
            val route = track.trackPoints.map(projection::project)
            val markers = track.markers.map { projection.project(it.point) }

            val contentRect = markers.fold(route.bounds().inflate(ROUTE_STROKE_WIDTH / 2)) { rect, center ->
                rect.union(Rect(center = center, radius = MARKER_SIZE / 2))
            }
            val origin = Offset(contentRect.left - STICKER_INSET, contentRect.top - STICKER_INSET)
            val size = Size(contentRect.width + STICKER_INSET * 2, contentRect.height + STICKER_INSET * 2)

            return PhotoTimelineStickerLayout(
                size = size,
                position = Offset(origin.x, canvasSize.height - size.height - STICKER_BOTTOM_OFFSET),
                routePoints = route.map { it - origin }.toImmutableList(),
                markerCenters = markers.map { it - origin }.toImmutableList(),
            )
        }
    }
}

/**
 * 위도·경도를 캔버스 좌표로 바꾼다. 북쪽이 위
 */
internal class TrackProjection private constructor(
    private val minLatitude: Double,
    private val maxLatitude: Double,
    private val minLongitude: Double,
    private val scale: Double,
    private val offset: Offset,
) {
    fun project(point: RecapTrackUiModel.GeoPoint): Offset = Offset(
        x = ((point.longitude - minLongitude) * scale).toFloat() + offset.x,
        y = ((maxLatitude - point.latitude) * scale).toFloat() + offset.y,
    )

    companion object {
        fun create(
            points: List<RecapTrackUiModel.GeoPoint>,
            canvasSize: Size,
            padding: Float,
        ): TrackProjection? {
            if (points.size < 2) return null
            val minLatitude = points.minOf { it.latitude }
            val maxLatitude = points.maxOf { it.latitude }
            val minLongitude = points.minOf { it.longitude }
            val maxLongitude = points.maxOf { it.longitude }
            val latitudeRange = maxLatitude - minLatitude
            val longitudeRange = maxLongitude - minLongitude
            if (latitudeRange <= 0 || longitudeRange <= 0) return null

            val scale = min(
                (canvasSize.width - padding * 2) / longitudeRange,
                (canvasSize.height - padding * 2) / latitudeRange,
            )
            val offset = Offset(
                x = ((canvasSize.width - longitudeRange * scale) / 2).toFloat(),
                y = ((canvasSize.height - latitudeRange * scale) / 2).toFloat(),
            )
            return TrackProjection(minLatitude, maxLatitude, minLongitude, scale, offset)
        }
    }
}

private fun List<Offset>.bounds(): Rect = Rect(
    left = minOf { it.x },
    top = minOf { it.y },
    right = maxOf { it.x },
    bottom = maxOf { it.y },
)

private fun Rect.union(other: Rect): Rect = Rect(
    left = min(left, other.left),
    top = min(top, other.top),
    right = maxOf(right, other.right),
    bottom = maxOf(bottom, other.bottom),
)
