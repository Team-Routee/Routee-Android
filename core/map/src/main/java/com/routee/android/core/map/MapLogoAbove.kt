package com.routee.android.core.map

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import kotlin.math.roundToInt

/**
 * 이 컴포넌트 위에 네이버 로고를 붙인다. [RouteeNaverMap] 의 content 안에서만 동작
 *
 * @param spacing   컴포넌트 위쪽과 로고 사이 간격
 * @param alignment 컴포넌트의 어느 쪽 끝에 맞출지 ([Alignment.Start], [Alignment.End])
 * @param inset     그 끝에서 안쪽으로 들어가는 거리
 */
fun Modifier.mapLogoAbove(
    spacing: Dp = 0.dp,
    alignment: Alignment.Horizontal = Alignment.Start,
    inset: Dp = 0.dp,
): Modifier = this then MapLogoAboveElement(spacing, alignment == Alignment.End, inset)

internal val LocalMapLogoAnchor = staticCompositionLocalOf<MapLogoAnchor?> { null }

// mapLogoAbove 를 단 컴포넌트 위치
@Stable
internal class MapLogoAnchor {
    var bounds by mutableStateOf<Rect?>(null)
    var spacing by mutableStateOf(0.dp)
    var isEnd by mutableStateOf(false)
    var inset by mutableStateOf(0.dp)

    // 지도 기준 로고 위치(px)
    fun logoPlacement(mapBounds: Rect?, density: Density, layoutDirection: LayoutDirection): LogoPlacement? {
        val map = mapBounds ?: return null
        val target = bounds ?: return null
        val alignRight = isEnd == (layoutDirection == LayoutDirection.Ltr)
        return with(density) {
            LogoPlacement(
                alignRight = alignRight,
                horizontal = if (alignRight) {
                    (map.right - target.right + inset.toPx()).roundToInt()
                } else {
                    (target.left - map.left + inset.toPx()).roundToInt()
                },
                bottom = (map.bottom - target.top + spacing.toPx()).roundToInt(),
            )
        }
    }
}

internal data class LogoPlacement(
    val alignRight: Boolean,
    val horizontal: Int,
    val bottom: Int,
)

private data class MapLogoAboveElement(
    val spacing: Dp,
    val isEnd: Boolean,
    val inset: Dp,
) : ModifierNodeElement<MapLogoAboveNode>() {
    override fun create() = MapLogoAboveNode(spacing, isEnd, inset)

    override fun update(node: MapLogoAboveNode) {
        node.spacing = spacing
        node.isEnd = isEnd
        node.inset = inset
    }
}

private class MapLogoAboveNode(
    var spacing: Dp,
    var isEnd: Boolean,
    var inset: Dp,
) : Modifier.Node(),
    GlobalPositionAwareModifierNode,
    CompositionLocalConsumerModifierNode {
    private var anchor: MapLogoAnchor? = null
    private var placedBounds: Rect? = null

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        val anchor = currentValueOf(LocalMapLogoAnchor) ?: return
        val bounds = Rect(coordinates.positionInRoot(), coordinates.size.toSize())
        this.anchor = anchor
        placedBounds = bounds
        anchor.bounds = bounds
        anchor.spacing = spacing
        anchor.isEnd = isEnd
        anchor.inset = inset
    }

    override fun onDetach() {
        anchor?.let { if (it.bounds == placedBounds) it.bounds = null }
        anchor = null
        placedBounds = null
    }
}
