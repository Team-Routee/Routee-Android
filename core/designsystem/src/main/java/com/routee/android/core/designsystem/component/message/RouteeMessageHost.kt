package com.routee.android.core.designsystem.component.message

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import com.routee.android.core.designsystem.theme.RouteeTheme

private val ToastHorizontalPadding = 24.dp

/**
 * 전역 메시지를 그리는 자리
 *
 * 앱 루트 `Scaffold`의 `snackbarHost`에 넣는다. 토스트는 [routeeMessageAnchor]를 붙인 요소 위에 띄운다.
 * 여러 개면 가장 위 요소 위, 없으면 화면 하단에 띄운다. 키보드가 더 높이 올라와 있으면 키보드 위에 띄운다. 스낵바는 항상 화면 하단에 띄운다
 */
@Composable
fun RouteeMessageHost(
    state: RouteeMessageState,
    modifier: Modifier = Modifier,
) {
    val gap = RouteeTheme.spacing.Spacing16
    val bottomInsets = WindowInsets.navigationBars.union(WindowInsets.ime)

    AnimatedContent(
        targetState = state.currentMessage,
        transitionSpec = { (fadeIn() togetherWith fadeOut()).using(sizeTransform = null) },
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier.fillMaxWidth(),
        label = "RouteeMessageHost",
    ) { message ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { liveRegion = LiveRegionMode.Polite },
            contentAlignment = Alignment.BottomCenter,
        ) {
            when (message) {
                is RouteeMessage.Toast -> RouteeToast(
                    text = message.text,
                    modifier = Modifier.toastPosition(state = state, bottomInsets = bottomInsets, gap = gap),
                )

                is RouteeMessage.Snackbar -> RouteeSnackbar(
                    text = message.text,
                    modifier = Modifier
                        .windowInsetsPadding(bottomInsets)
                        .padding(start = gap, end = gap, bottom = gap)
                        .fillMaxWidth(),
                    actionLabel = message.actionLabel,
                    onActionClick = state::performAction,
                )

                null -> Unit
            }
        }
    }
}

private fun Modifier.toastPosition(
    state: RouteeMessageState,
    bottomInsets: WindowInsets,
    gap: Dp,
): Modifier = layout { measurable, constraints ->
    val horizontal = ToastHorizontalPadding.roundToPx()
    val anchorBottom = state.anchors.values.filter { it.isActive() }.maxOfOrNull { it.bottomOffset } ?: 0
    val bottom = maxOf(anchorBottom, bottomInsets.getBottom(this)) + gap.roundToPx()
    val placeable = measurable.measure(constraints.offset(horizontal = -horizontal * 2, vertical = -bottom))
    layout(placeable.width + horizontal * 2, placeable.height + bottom) {
        placeable.place(horizontal, 0)
    }
}

/**
 * 토스트를 이 요소 위에 띄우도록 지정
 *
 * 화면 하단 버튼이나 탭 바에 붙인다. 여러 개면 가장 위에 있는 요소 위 16dp에 띄운다.
 * 다이얼로그 · 바텀시트 안에서는 쓰지 않는다 (별도 창이라 위치가 맞지 않음)
 */
fun Modifier.routeeMessageAnchor(): Modifier = this then MessageAnchorElement

/**
 * 이 영역의 [routeeMessageAnchor]를 토스트 위치 계산에 쓸지 정하는 값
 *
 * 화면 전환으로 사라지는 중인 화면은 false 를 돌려주게 제공한다
 *
 * ```kotlin
 * val scope = LocalNavAnimatedContentScope.current
 * CompositionLocalProvider(
 *     LocalRouteeMessageAnchorActive provides { scope.transition.targetState != EnterExitState.PostExit },
 * ) { content() }
 * ```
 */
val LocalRouteeMessageAnchorActive = staticCompositionLocalOf<() -> Boolean> { { true } }

internal class MessageAnchor(
    val bottomOffset: Int,
    val isActive: () -> Boolean,
)

private data object MessageAnchorElement : ModifierNodeElement<MessageAnchorNode>() {
    override fun create() = MessageAnchorNode()

    override fun update(node: MessageAnchorNode) = Unit
}

private class MessageAnchorNode :
    Modifier.Node(),
    GlobalPositionAwareModifierNode,
    CompositionLocalConsumerModifierNode {
    private var state: RouteeMessageState? = null

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        val messageState = state ?: currentValueOf(LocalRouteeMessageState).also { state = it }
        val rootHeight = coordinates.findRootCoordinates().size.height
        val bottomOffset = rootHeight - coordinates.positionInRoot().y.toInt()
        val isActive = currentValueOf(LocalRouteeMessageAnchorActive)
        val anchor = messageState.anchors[this]
        if (anchor?.bottomOffset != bottomOffset || anchor.isActive !== isActive) {
            messageState.anchors[this] = MessageAnchor(bottomOffset = bottomOffset, isActive = isActive)
        }
    }

    override fun onDetach() {
        state?.anchors?.remove(this)
        state = null
    }
}
