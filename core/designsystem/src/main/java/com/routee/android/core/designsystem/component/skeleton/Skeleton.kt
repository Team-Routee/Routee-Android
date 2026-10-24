package com.routee.android.core.designsystem.component.skeleton

import androidx.compose.animation.core.EaseInOut
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.InspectorInfo
import com.routee.android.core.designsystem.theme.RouteePalette
import com.routee.android.core.designsystem.theme.RouteeRadius
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

// 스켈레톤 UI 시각적 규칙: grey800 바탕, grey600 띠(양 끝 grey800 0%), 띠 폭 50%, 왼쪽 → 오른쪽, ease-in-out 1200ms 반복
private const val SHIMMER_DURATION_MS = 1_200L
private const val SHIMMER_BAND_WIDTH_FRACTION = 0.5f

private val ShimmerBandColors = easedGradientColors(RouteePalette.Grey800, RouteePalette.Grey600, FigmaAlphaFadeEasing)
    .let { rising -> rising + rising.asReversed().drop(1) }

/**
 * 스켈레톤 블록 (shimmer_gradient)
 *
 * @param shape    대신하는 컴포넌트의 모서리
 * @param animated false 이면 띠 없이 바탕만
 */
fun Modifier.skeleton(
    shape: Shape = RoundedCornerShape(RouteeRadius.Radius8),
    animated: Boolean = true,
): Modifier = clip(shape) then SkeletonElement(animated)

private data class SkeletonElement(
    private val animated: Boolean,
) : ModifierNodeElement<SkeletonNode>() {
    override fun create() = SkeletonNode(animated)

    override fun update(node: SkeletonNode) {
        node.update(animated)
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "skeleton"
        properties["animated"] = animated
    }
}

private class SkeletonNode(
    private var animated: Boolean,
) : Modifier.Node(),
    DrawModifierNode {
    private var progress = 0f
    private var shimmerJob: Job? = null
    private var bandWidth = 0f
    private var bandBrush: Brush? = null

    override fun onAttach() {
        startShimmer()
    }

    override fun onDetach() {
        shimmerJob = null
    }

    fun update(animated: Boolean) {
        if (this.animated == animated) return
        this.animated = animated
        shimmerJob?.cancel()
        shimmerJob = null
        if (isAttached) startShimmer()
        invalidateDraw()
    }

    // 진행률을 프레임 시각으로 계산해서 따로 붙은 블록끼리도 띠가 같이 움직임
    private fun startShimmer() {
        if (!animated) return
        shimmerJob = coroutineScope.launch {
            while (isActive) {
                withFrameMillis { frameMillis ->
                    progress = EaseInOut.transform((frameMillis % SHIMMER_DURATION_MS) / SHIMMER_DURATION_MS.toFloat())
                }
                invalidateDraw()
            }
        }
    }

    override fun ContentDrawScope.draw() {
        drawRect(RouteePalette.Grey800)
        if (animated) {
            val width = size.width * SHIMMER_BAND_WIDTH_FRACTION
            val brush = bandBrush?.takeIf { bandWidth == width }
                ?: Brush.horizontalGradient(ShimmerBandColors, startX = 0f, endX = width).also {
                    bandWidth = width
                    bandBrush = it
                }
            // 띠가 왼쪽 바깥에서 들어와 오른쪽 바깥으로 빠져나감
            translate(left = -width + (size.width + width) * progress) {
                drawRect(brush, size = Size(width, size.height))
            }
        }
        drawContent()
    }
}
