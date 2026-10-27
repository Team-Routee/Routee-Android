package com.routee.android.feature.recordedit.component.editor

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size

/**
 * 크기 조절 핸들 위치. 오른쪽 위는 삭제 버튼
 */
internal enum class StickerHandle {
    TOP_START,
    BOTTOM_START,
    BOTTOM_END,
}

/**
 * 스티커 이동, 크기 조절 계산 (RT-EDIT-007). 모든 값은 dp
 */
internal object StickerGeometry {
    const val MIN_VISIBLE_LENGTH = 20f
    private const val MIN_SCALE = 0.01f

    fun move(
        start: Rect,
        drag: Offset,
        canvasSize: Size,
    ): Rect = clampVisible(start.translate(drag), canvasSize)

    /**
     * 비율을 유지한 채 [handle] 반대쪽 모서리를 고정하고 크기를 바꾼다
     *
     * [drag]를 고정 모서리에서 핸들로 향하는 대각선에 투영한 만큼 커지거나 작아진다
     */
    fun resize(
        start: Rect,
        handle: StickerHandle,
        drag: Offset,
        canvasSize: Size,
    ): Rect {
        val isStart = handle != StickerHandle.BOTTOM_END
        val isTop = handle == StickerHandle.TOP_START
        val diagonal = Offset(
            x = if (isStart) -start.width else start.width,
            y = if (isTop) -start.height else start.height,
        )
        val projection = (drag.x * diagonal.x + drag.y * diagonal.y) / diagonal.getDistanceSquared()
        val scale = (1 + projection).coerceAtLeast(MIN_SCALE)
        val size = start.size * scale
        val topLeft = Offset(
            x = if (isStart) start.right - size.width else start.left,
            y = if (isTop) start.bottom - size.height else start.top,
        )
        return clampVisible(Rect(topLeft, size), canvasSize)
    }

    fun center(
        size: Size,
        canvasSize: Size,
    ): Rect = Rect(
        offset = Offset((canvasSize.width - size.width) / 2, (canvasSize.height - size.height) / 2),
        size = size,
    )

    private fun clampVisible(
        rect: Rect,
        canvasSize: Size,
    ): Rect {
        val left = rect.left
            .coerceAtMost(canvasSize.width - MIN_VISIBLE_LENGTH)
            .coerceAtLeast(MIN_VISIBLE_LENGTH - rect.width)
        val top = rect.top
            .coerceAtMost(canvasSize.height - MIN_VISIBLE_LENGTH)
            .coerceAtLeast(MIN_VISIBLE_LENGTH - rect.height)
        return Rect(Offset(left, top), rect.size)
    }
}
