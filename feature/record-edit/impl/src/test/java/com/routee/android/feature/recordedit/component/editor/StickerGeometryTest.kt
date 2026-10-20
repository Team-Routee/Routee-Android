package com.routee.android.feature.recordedit.component.editor

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import org.junit.Assert.assertEquals
import org.junit.Test

class StickerGeometryTest {

    /**
     * GIVEN: 캔버스 안 스티커를
     * WHEN: 옮기면
     * THEN: 이동한 만큼 위치가 바뀐다
     */
    @Test
    fun `옮긴 만큼 위치가 바뀐다`() {
        val moved = StickerGeometry.move(STICKER, Offset(30f, -20f), CANVAS)

        assertEquals(Rect(Offset(130f, 80f), Size(100f, 50f)), moved)
    }

    /**
     * GIVEN: 스티커를 캔버스 밖으로 멀리
     * WHEN: 옮기면
     * THEN: 캔버스 안에 20만큼 남는다
     */
    @Test
    fun `캔버스 밖으로 옮겨도 20은 남는다`() {
        val toTopLeft = StickerGeometry.move(STICKER, Offset(-1000f, -1000f), CANVAS)
        val toBottomRight = StickerGeometry.move(STICKER, Offset(1000f, 1000f), CANVAS)

        assertEquals(Offset(-80f, -30f), toTopLeft.topLeft)
        assertEquals(Offset(CANVAS.width - 20f, CANVAS.height - 20f), toBottomRight.topLeft)
    }

    /**
     * GIVEN: 오른쪽 아래 핸들을
     * WHEN: 오른쪽으로 끌면
     * THEN: 왼쪽 위를 고정한 채 비율을 유지하며 커진다
     */
    @Test
    fun `오른쪽 아래 핸들은 왼쪽 위를 고정하고 비율을 유지한다`() {
        val resized = StickerGeometry.resize(STICKER, StickerHandle.BOTTOM_END, Offset(50f, 0f), CANVAS)

        assertEquals(Rect(Offset(100f, 100f), Size(150f, 75f)), resized)
    }

    /**
     * GIVEN: 왼쪽 위 핸들을
     * WHEN: 오른쪽 아래로 끌면
     * THEN: 오른쪽 아래를 고정한 채 작아진다
     */
    @Test
    fun `왼쪽 위 핸들은 오른쪽 아래를 고정한다`() {
        val resized = StickerGeometry.resize(STICKER, StickerHandle.TOP_START, Offset(50f, 10f), CANVAS)

        assertEquals(Rect(Offset(150f, 125f), Size(50f, 25f)), resized)
    }

    /**
     * GIVEN: 왼쪽 아래 핸들을
     * WHEN: 아래로만 끌면
     * THEN: 오른쪽 위를 고정한 채 높이 변화만큼 커진다
     */
    @Test
    fun `왼쪽 아래 핸들은 오른쪽 위를 고정한다`() {
        val resized = StickerGeometry.resize(STICKER, StickerHandle.BOTTOM_START, Offset(0f, 25f), CANVAS)

        assertEquals(Rect(Offset(50f, 100f), Size(150f, 75f)), resized)
    }

    /**
     * GIVEN: 스티커 크기와 캔버스 크기로
     * WHEN: 가운데 위치를 구하면
     * THEN: 스티커 중심이 캔버스 중심과 같다
     */
    @Test
    fun `가운데 배치는 중심이 캔버스 중심이다`() {
        val centered = StickerGeometry.center(Size(100f, 50f), CANVAS)

        assertEquals(Offset(CANVAS.width / 2, CANVAS.height / 2), centered.center)
    }

    private companion object {
        val CANVAS = Size(328f, 583f)
        val STICKER = Rect(Offset(100f, 100f), Size(100f, 50f))
    }
}
