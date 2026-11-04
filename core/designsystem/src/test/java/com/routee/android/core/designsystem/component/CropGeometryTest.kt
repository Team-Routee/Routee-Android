package com.routee.android.core.designsystem.component

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private const val DELTA = 0.01f

class CropGeometryTest {
    private val bounds = Rect(0f, 0f, 900f, 1600f)
    private val geometry = CropGeometry.initial(
        contentBounds = bounds,
        aspectRatio = 9f / 16f,
        imageSize = Size(4000f, 3000f),
    )

    @Test
    fun `처음에는 박스가 영역에 맞고 사진이 박스를 덮는다`() {
        assertEquals(bounds, geometry.box)
        assertEquals(1600f / 3000f, geometry.scale, DELTA)
        assertEquals(bounds.center, geometry.center)
    }

    @Test
    fun `벌리면 박스는 그대로 두고 사진만 커진다`() {
        val zoomed = geometry.transformed(geometry.box.center, Offset.Zero, zoom = 2f)

        assertEquals(geometry.scale * 2f, zoomed.scale, DELTA)
        assertEquals(geometry.box, zoomed.box)
    }

    @Test
    fun `오므리면 사진이 작아지되 박스를 덮는 배율 아래로 내려가지 않는다`() {
        val zoomed = geometry.transformed(geometry.box.center, Offset.Zero, zoom = 4f)

        val zoomedOut = zoomed.transformed(zoomed.box.center, Offset.Zero, zoom = 0.5f)
        val minimum = zoomedOut.transformed(zoomed.box.center, Offset.Zero, zoom = 0.01f)

        assertEquals(geometry.scale * 2f, zoomedOut.scale, DELTA)
        assertEquals(geometry.scale, minimum.scale, DELTA)
    }

    @Test
    fun `핀치 중심의 사진 위치는 확대해도 유지된다`() {
        val centroid = Offset(600f, 500f)

        val zoomed = geometry.transformed(centroid, Offset.Zero, zoom = 1.5f)

        assertEquals(
            (centroid.x - geometry.center.x) / geometry.scale,
            (centroid.x - zoomed.center.x) / zoomed.scale,
            DELTA,
        )
        assertEquals(
            (centroid.y - geometry.center.y) / geometry.scale,
            (centroid.y - zoomed.center.y) / zoomed.scale,
            DELTA,
        )
    }

    @Test
    fun `벌리면서 옮기면 처음 잡은 사진 위치가 손가락을 따라간다`() {
        val previousCentroid = Offset(600f, 500f)
        val pan = Offset(20f, 10f)

        val zoomed = geometry.transformed(previousCentroid + pan, pan, zoom = 1.5f)

        assertEquals(
            (previousCentroid.x - geometry.center.x) / geometry.scale,
            (previousCentroid.x + pan.x - zoomed.center.x) / zoomed.scale,
            DELTA,
        )
        assertEquals(
            (previousCentroid.y - geometry.center.y) / geometry.scale,
            (previousCentroid.y + pan.y - zoomed.center.y) / zoomed.scale,
            DELTA,
        )
    }

    @Test
    fun `사진을 옮겨도 박스 밖으로 사진이 빠지지 않는다`() {
        val panned = geometry.transformed(geometry.box.center, Offset(100000f, 100000f), zoom = 1f)

        assertEquals(geometry.box.left, panned.photoRect.left, DELTA)
        assertEquals(geometry.box.top, panned.photoRect.top, DELTA)
    }

    @Test
    fun `모서리를 끌면 비율을 유지한 채 반대쪽 모서리 기준으로 줄어든다`() {
        val resized = geometry.resized(CropHandle.BottomRight, Offset(450f, 800f))

        assertEquals(0f, resized.box.left, DELTA)
        assertEquals(0f, resized.box.top, DELTA)
        assertEquals(450f, resized.box.width, DELTA)
        assertEquals(800f, resized.box.height, DELTA)
        assertFalse(resized.isSettled)
    }

    @Test
    fun `박스는 최소 크기보다 작아지지 않고 영역 밖으로 커지지 않는다`() {
        val smallest = geometry.resized(CropHandle.BottomRight, Offset(1f, 1f))
        val largest = smallest.resized(CropHandle.BottomRight, Offset(5000f, 5000f))

        assertEquals(900f * 0.25f, smallest.box.width, DELTA)
        assertEquals(bounds, largest.box)
    }

    @Test
    fun `박스를 다시 가운데로 맞춰도 보이는 사진 영역은 같다`() {
        val resized = geometry.resized(CropHandle.BottomRight, Offset(450f, 800f))
        val before = visibleImageRegion(resized)

        val recentered = resized.recentered()
        val after = visibleImageRegion(recentered)

        assertEquals(bounds, recentered.box)
        assertEquals(before.left, after.left, DELTA)
        assertEquals(before.top, after.top, DELTA)
        assertEquals(before.width, after.width, DELTA)
        assertEquals(before.height, after.height, DELTA)
        assertTrue(recentered.isSettled)
    }

    @Test
    fun `회전하면 가로세로가 바뀌어도 사진이 박스를 덮는다`() {
        val rotated = geometry.rotated(clockwise = true)

        assertEquals(1, rotated.quarterTurns)
        assertTrue(rotated.photoRect.left <= rotated.box.left + DELTA)
        assertTrue(rotated.photoRect.right >= rotated.box.right - DELTA)
        assertTrue(rotated.photoRect.top <= rotated.box.top + DELTA)
        assertTrue(rotated.photoRect.bottom >= rotated.box.bottom - DELTA)
        assertEquals(3, geometry.rotated(clockwise = false).quarterTurns)
    }

    @Test
    fun `초기화하면 회전과 배율이 처음으로 돌아간다`() {
        val changed = geometry
            .transformed(geometry.box.center, Offset.Zero, zoom = 3f)
            .rotated(clockwise = true)

        assertEquals(geometry, changed.reset())
    }

    @Test
    fun `터치 위치로 모서리와 변을 구분하고 안쪽은 핸들이 아니다`() {
        val inset = CropGeometry.initial(
            contentBounds = Rect(100f, 100f, 1000f, 1700f),
            aspectRatio = 9f / 16f,
            imageSize = Size(4000f, 3000f),
        )

        assertEquals(CropHandle.TopLeft, inset.handleAt(Offset(105f, 95f), touchRadius = 24f))
        assertEquals(CropHandle.Right, inset.handleAt(Offset(1000f, 900f), touchRadius = 24f))
        assertNull(inset.handleAt(inset.box.center, touchRadius = 24f))
        assertNull(inset.handleAt(Offset(10f, 10f), touchRadius = 24f))
    }

    private fun visibleImageRegion(geometry: CropGeometry): Rect {
        val photo = geometry.photoRect
        val box = geometry.box
        return Rect(
            left = (box.left - photo.left) / geometry.scale,
            top = (box.top - photo.top) / geometry.scale,
            right = (box.right - photo.left) / geometry.scale,
            bottom = (box.bottom - photo.top) / geometry.scale,
        )
    }
}
