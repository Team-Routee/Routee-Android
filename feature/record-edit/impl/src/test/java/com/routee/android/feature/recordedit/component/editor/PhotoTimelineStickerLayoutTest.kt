package com.routee.android.feature.recordedit.component.editor

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.routee.android.feature.recordedit.editor.model.RecapTrackUiModel
import com.routee.android.feature.recordedit.editor.model.RecapTrackUiModel.GeoPoint
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PhotoTimelineStickerLayoutTest {

    /**
     * GIVEN: 남쪽에서 북쪽으로 가는 경로가 있을 때
     * WHEN: 캔버스 좌표로 바꾸면
     * THEN: 북쪽이 위(y가 작음)다
     */
    @Test
    fun `북쪽이 위로 그려진다`() {
        val projection = projection(GeoPoint(37.50, 127.00), GeoPoint(37.60, 127.10))

        val south = projection.project(GeoPoint(37.50, 127.00))
        val north = projection.project(GeoPoint(37.60, 127.10))

        assertTrue(north.y < south.y)
        assertTrue(north.x > south.x)
    }

    /**
     * GIVEN: 세로로 긴 경로가 있을 때
     * WHEN: 캔버스 좌표로 바꾸면
     * THEN: 가로세로 비율을 유지한 채 여백 안에서 가운데에 놓인다
     */
    @Test
    fun `비율을 유지하며 여백 안 가운데에 놓인다`() {
        val projection = projection(GeoPoint(37.50, 127.00), GeoPoint(37.90, 127.10))

        val topRight = projection.project(GeoPoint(37.90, 127.10))
        val bottomLeft = projection.project(GeoPoint(37.50, 127.00))

        assertEquals(PADDING, topRight.y, DELTA)
        assertEquals(CANVAS.height - PADDING, bottomLeft.y, DELTA)
        assertEquals(CANVAS.width / 2, (topRight.x + bottomLeft.x) / 2, DELTA)
        assertEquals(0.25f, (topRight.x - bottomLeft.x) / (bottomLeft.y - topRight.y), DELTA)
    }

    /**
     * GIVEN: 경로 좌표가 2개 미만이거나 한 줄로만 있을 때
     * WHEN: 스티커 배치를 만들면
     * THEN: 스티커가 없다 (null)
     */
    @Test
    fun `그릴 수 없는 경로는 스티커가 없다`() {
        assertNull(layout(GeoPoint(37.5, 127.0)))
        assertNull(layout(GeoPoint(37.5, 127.0), GeoPoint(37.5, 127.1)))
    }

    /**
     * GIVEN: 사진이 있는 경로일 때
     * WHEN: 스티커 배치를 만들면
     * THEN: 사진은 경로 위 좌표에 놓이고, 스티커는 캔버스 아래에서 36dp 위에 놓인다
     */
    @Test
    fun `사진은 경로 좌표에 놓이고 스티커는 캔버스 아래쪽에 놓인다`() {
        val start = GeoPoint(37.50, 127.00)
        val end = GeoPoint(37.60, 127.05)
        val track = RecapTrackUiModel(
            trackPoints = persistentListOf(start, end),
            markers = persistentListOf(RecapTrackUiModel.TimelineMarker(1L, "url", end)),
        )

        val layout = requireNotNull(PhotoTimelineStickerLayout.create(track, CANVAS))

        assertEquals(layout.routePoints.last(), layout.markerCenters.single())
        assertEquals(CANVAS.height - 36f, layout.position.y + layout.size.height, DELTA)
        assertTrue(layout.routePoints.all { it.isInside(layout.size) })
    }

    private fun projection(vararg points: GeoPoint) =
        requireNotNull(TrackProjection.create(points.toList(), CANVAS, PADDING))

    private fun layout(vararg points: GeoPoint) = PhotoTimelineStickerLayout.create(
        RecapTrackUiModel(trackPoints = points.toList().toImmutableList(), markers = persistentListOf()),
        CANVAS,
    )

    private fun Offset.isInside(size: Size) = x in 0f..size.width && y in 0f..size.height

    private companion object {
        val CANVAS = Size(328f, 583f)
        const val PADDING = 60f
        const val DELTA = 0.01f
    }
}
