package com.routee.android.feature.recordedit.editor.model

import androidx.compose.runtime.Immutable
import com.routee.android.feature.recordedit.model.StickerType

/**
 * 캔버스에 놓인 스티커
 *
 * @param type        스티커 종류
 * @param isCentered  true 이면 처음 자리 대신 가운데에 놓는다. 삭제 후 다시 추가하거나 루트일 때
 * @param frame       옮기거나 크기를 바꾼 위치. null 이면 처음 자리
 */
@Immutable
internal data class RecapSticker(
    val type: StickerType,
    val isCentered: Boolean = false,
    val frame: StickerFrame? = null,
)

/**
 * @param x      캔버스 안 왼쪽 (dp)
 * @param y      캔버스 안 위쪽 (dp)
 * @param scale  처음 크기 대비 배율
 */
@Immutable
internal data class StickerFrame(
    val x: Float,
    val y: Float,
    val scale: Float,
)
