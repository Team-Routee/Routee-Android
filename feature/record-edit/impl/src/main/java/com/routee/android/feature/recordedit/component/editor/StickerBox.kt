package com.routee.android.feature.recordedit.component.editor

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.R as DesignSystemR
import com.routee.android.core.designsystem.extension.noRippleClickable
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.feature.recordedit.R
import com.routee.android.feature.recordedit.editor.model.StickerFrame

// sticker_bounding_box
private val BorderWidth = 1.dp

/**
 * 캔버스 위 스티커 틀 (sticker_bounding_box)
 *
 * 캔버스 크기를 채우고, 그 안에서 [content]를 [frame] 위치와 배율로 놓는다
 *
 * @param frame            옮기거나 크기를 바꾼 위치. null 이면 [initialPosition]
 * @param initialPosition  처음 자리 (dp). null 이면 캔버스 가운데
 * @param isSelected       true 이면 테두리, 크기 조절 핸들, 삭제 버튼 표시. 이동과 크기 조절도 이때만 가능
 * @param onClick          스티커 탭 시 호출
 * @param onFrameChange    이동하거나 크기를 바꿀 때 호출
 * @param onDeleteClick    삭제 버튼 탭 시 호출
 * @param content          스티커 내용. 테두리와의 여백 포함
 */
@Composable
internal fun StickerBox(
    frame: StickerFrame?,
    initialPosition: Offset?,
    isSelected: Boolean,
    onClick: () -> Unit,
    onFrameChange: (StickerFrame) -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val bounds = remember { StickerBounds() }
    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnFrameChange by rememberUpdatedState(onFrameChange)

    Layout(
        contents = listOf(
            content,
            {
                Box(
                    modifier = Modifier
                        .pointerInput(Unit) { detectTapGestures { currentOnClick() } }
                        .then(
                            if (isSelected) {
                                Modifier.pointerInput(Unit) {
                                    var start = Rect.Zero
                                    var drag = Offset.Zero
                                    detectDragGestures(
                                        onDragStart = {
                                            start = bounds.rect
                                            drag = Offset.Zero
                                        },
                                        onDrag = { change, amount ->
                                            change.consume()
                                            drag += amount / density
                                            val rect = StickerGeometry.move(start, drag, bounds.canvasSize)
                                            currentOnFrameChange(bounds.frameOf(rect))
                                        },
                                    )
                                }
                            } else {
                                Modifier
                            },
                        ),
                )
            },
            {
                if (isSelected) {
                    StickerDecoration(
                        onResize = { handle, start, drag ->
                            val rect = StickerGeometry.resize(start, handle, drag, bounds.canvasSize)
                            currentOnFrameChange(bounds.frameOf(rect))
                        },
                        currentRect = { bounds.rect },
                        onDeleteClick = onDeleteClick,
                    )
                }
            },
        ),
        modifier = modifier,
    ) { (contentMeasurables, touchMeasurables, decorationMeasurables), constraints ->
        val contentPlaceable = contentMeasurables.first().measure(Constraints())
        val baseSize = Size(contentPlaceable.width.toDp().value, contentPlaceable.height.toDp().value)
        val canvasSize = Size(constraints.maxWidth.toDp().value, constraints.maxHeight.toDp().value)
        val rect = when {
            frame != null -> Rect(Offset(frame.x, frame.y), baseSize * frame.scale)
            initialPosition != null -> Rect(initialPosition, baseSize)
            else -> StickerGeometry.center(baseSize, canvasSize)
        }
        bounds.update(rect = rect, baseWidth = baseSize.width, canvasSize = canvasSize)

        val left = rect.left.dp.roundToPx()
        val top = rect.top.dp.roundToPx()
        val width = rect.width.dp.roundToPx()
        val height = rect.height.dp.roundToPx()
        val margin = RouteeTheme.spacing.Spacing12.roundToPx()
        val scale = frame?.scale ?: 1f
        val touchPlaceable = touchMeasurables.first().measure(Constraints.fixed(width, height))
        val decorationPlaceables = decorationMeasurables.map {
            it.measure(Constraints.fixed(width + margin * 2, height + margin * 2))
        }

        layout(constraints.maxWidth, constraints.maxHeight) {
            contentPlaceable.placeWithLayer(left, top) {
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin(0f, 0f)
            }
            touchPlaceable.place(left, top)
            decorationPlaceables.forEach { it.place(left - margin, top - margin) }
        }
    }
}

@Composable
private fun StickerDecoration(
    onResize: (handle: StickerHandle, start: Rect, drag: Offset) -> Unit,
    currentRect: () -> Rect,
    onDeleteClick: () -> Unit,
) {
    val currentOnResize by rememberUpdatedState(onResize)

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(RouteeTheme.spacing.Spacing12)
                .border(BorderWidth, RouteeTheme.colors.statusInfo),
        )
        StickerHandle.entries.forEach { handle ->
            ResizeHandle(
                alignment = handle.alignment,
                onDrag = { start, drag -> currentOnResize(handle, start, drag) },
                currentRect = currentRect,
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(RouteeTheme.spacing.Spacing24)
                .clip(CircleShape)
                .background(RouteeTheme.colors.statusInfo)
                .noRippleClickable(role = Role.Button, onClick = onDeleteClick),
        ) {
            Image(
                imageVector = ImageVector.vectorResource(DesignSystemR.drawable.ic_cancel_sm_outline),
                contentDescription = stringResource(R.string.record_edit_editor_sticker_delete_a11y),
            )
        }
    }
}

@Composable
private fun BoxScope.ResizeHandle(
    alignment: Alignment,
    onDrag: (start: Rect, drag: Offset) -> Unit,
    currentRect: () -> Rect,
) {
    val currentOnDrag by rememberUpdatedState(onDrag)

    Box(
        modifier = Modifier
            .align(alignment)
            .size(RouteeTheme.spacing.Spacing24)
            .pointerInput(Unit) {
                var start = Rect.Zero
                var drag = Offset.Zero
                detectDragGestures(
                    onDragStart = {
                        start = currentRect()
                        drag = Offset.Zero
                    },
                    onDrag = { change, amount ->
                        change.consume()
                        drag += amount / density
                        currentOnDrag(start, drag)
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(RouteeTheme.spacing.Spacing8)
                .background(RouteeTheme.colors.white)
                .border(BorderWidth, RouteeTheme.colors.statusInfo),
        )
    }
}

private val StickerHandle.alignment: Alignment
    get() = when (this) {
        StickerHandle.TOP_START -> Alignment.TopStart
        StickerHandle.BOTTOM_START -> Alignment.BottomStart
        StickerHandle.BOTTOM_END -> Alignment.BottomEnd
    }

// 측정할 때 정한 위치를 제스처에서 읽는다
private class StickerBounds {
    var rect: Rect = Rect.Zero
        private set
    var canvasSize: Size = Size.Zero
        private set
    private var baseWidth: Float = 0f

    fun update(
        rect: Rect,
        baseWidth: Float,
        canvasSize: Size,
    ) {
        this.rect = rect
        this.baseWidth = baseWidth
        this.canvasSize = canvasSize
    }

    fun frameOf(rect: Rect): StickerFrame = StickerFrame(
        x = rect.left,
        y = rect.top,
        scale = if (baseWidth > 0f) rect.width / baseWidth else 1f,
    )
}
