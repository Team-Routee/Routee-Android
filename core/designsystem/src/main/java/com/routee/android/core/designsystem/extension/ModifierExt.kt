package com.routee.android.core.designsystem.extension

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.selection.selectable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusEventModifierNode
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.relocation.BringIntoViewModifierNode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 리플 효과 없이 클릭 가능하게 만드는 Modifier
 *
 * @param enabled 클릭 가능 여부
 * @param role 접근성 역할 (예: [Role.Button])
 * @param onClick 클릭 시 실행될 콜백
 */
fun Modifier.noRippleClickable(
    enabled: Boolean = true,
    role: Role? = null,
    onClick: () -> Unit,
): Modifier = clickable(
    interactionSource = null,
    indication = null,
    enabled = enabled,
    role = role,
    onClick = onClick,
)

/**
 * 리플 효과 없이 선택 가능하게 만드는 Modifier
 *
 * @param selected 선택 여부
 * @param role 접근성 역할 (예: [Role.RadioButton], [Role.Tab])
 * @param enabled 선택 가능 여부
 * @param onClick 클릭 시 실행될 콜백
 */
fun Modifier.noRippleSelectable(
    selected: Boolean,
    role: Role? = null,
    enabled: Boolean = true,
    onClick: () -> Unit,
): Modifier = selectable(
    selected = selected,
    interactionSource = null,
    indication = null,
    enabled = enabled,
    role = role,
    onClick = onClick,
)

// TODO: 화면 작업 시 imePadding + 스크롤 컨테이너로 부족한 경우에만 적용 (적용 전 코드 재검토, Modifier.Node로 전환)

/**
 * 포커스된 컴포저블을 키보드에 가리지 않도록 컴포넌트 영역 안으로 이동시키는 함수
 * @param isFocused  bring-into-view 동작 실행 여부를 결정하는 상태 값
 * @param delayMillis  키보드 표시 이후 동작 실행까지 대기할 지연 시간 값(ms)
 */
@Suppress("ktlint:compose:modifier-composed-check")
fun Modifier.bringIntoViewOnFocus(
    isFocused: Boolean,
    extraBottom: Dp = 0.dp,
    delayMillis: Long = 200L,
): Modifier =
    composed {
        val bringIntoViewRequester = remember { BringIntoViewRequester() }
        var layoutCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

        val density = LocalDensity.current
        val imeBottom = WindowInsets.ime.getBottom(density)
        val extraBottomPx = with(density) { extraBottom.toPx() }

        LaunchedEffect(isFocused, imeBottom) {
            if (!isFocused || imeBottom <= 0) return@LaunchedEffect
            val coords = layoutCoordinates ?: return@LaunchedEffect

            delay(delayMillis)

            val targetRect =
                Rect(
                    left = 0f,
                    top = 0f,
                    right = coords.size.width.toFloat(),
                    bottom = coords.size.height + extraBottomPx,
                )

            bringIntoViewRequester.bringIntoView(targetRect)
        }

        this
            .bringIntoViewRequester(bringIntoViewRequester)
            .onGloballyPositioned { layoutCoordinates = it }
    }

/**
 * 화면의 빈 영역을 터치했을 때 포커스를 해제하는 함수
 * @param focusManager 현재 화면의 [FocusManager] 객체
 * @param doOnClear 포커스 해제 시 추가로 실행할 동작
 */
fun Modifier.clearFocus(
    focusManager: FocusManager,
    doOnClear: () -> Unit = {},
): Modifier =
    this.pointerInput(focusManager, doOnClear) {
        detectTapGestures(
            onTap = {
                doOnClear()
                focusManager.clearFocus()
            },
        )
    }

// TODO: 화면 작업 시 imePadding + 스크롤 컨테이너로 부족한 경우에만 적용 (적용 전 코드 재검토)

/**
 * 텍스트 입력 시 커서 위치로 인한 자동 스크롤을 방지
 * BasicTextField의 기본 bringIntoView 동작을 차단
 */
fun Modifier.preventCursorScroll(): Modifier = this then PreventBringIntoViewElement

private data object PreventBringIntoViewElement : ModifierNodeElement<PreventBringIntoViewNode>() {
    override fun create(): PreventBringIntoViewNode = PreventBringIntoViewNode()

    override fun update(node: PreventBringIntoViewNode) = Unit
}

/**
 * 자식의 bringIntoView 요청을 받아 부모로 전달하지 않는다
 */
private class PreventBringIntoViewNode :
    Modifier.Node(),
    BringIntoViewModifierNode {
    override suspend fun bringIntoView(
        childCoordinates: LayoutCoordinates,
        boundsProvider: () -> Rect?,
    ) = Unit
}

/**
 * 키보드 높이에서 [bottomHeight]만큼 뺀 만큼만 아래 여백을 준다
 *
 * 키보드에 가려져도 되는 하단 영역(버튼 등) 위의 스크롤 영역에 붙인다. 바깥에서 소비한 인셋(내비게이션 바)은 빼고 계산된다
 *
 * @param bottomHeight 스크롤 영역 아래에 있는 하단 영역 높이
 */
fun Modifier.imePaddingAbove(bottomHeight: Dp): Modifier =
    consumeWindowInsets(PaddingValues(bottom = bottomHeight)).imePadding()

/**
 * 안쪽에 포커스가 있는 동안 스크롤을 끝까지 내린다
 *
 * 키보드가 올라오며 영역이 줄거나 에러 문구가 생겨 길이가 바뀌어도 다시 끝으로 맞춘다. `verticalScroll` 앞에 붙인다
 */
fun Modifier.scrollToEndOnFocus(scrollState: ScrollState): Modifier = this then ScrollToEndOnFocusElement(scrollState)

private data class ScrollToEndOnFocusElement(
    private val scrollState: ScrollState,
) : ModifierNodeElement<ScrollToEndOnFocusNode>() {
    override fun create(): ScrollToEndOnFocusNode = ScrollToEndOnFocusNode(scrollState)

    override fun update(node: ScrollToEndOnFocusNode) {
        node.update(scrollState)
    }
}

private class ScrollToEndOnFocusNode(
    private var scrollState: ScrollState,
) : Modifier.Node(),
    FocusEventModifierNode {
    private var scrollJob: Job? = null

    override fun onFocusEvent(focusState: FocusState) {
        if (!focusState.hasFocus) {
            scrollJob?.cancel()
            scrollJob = null
            return
        }
        if (scrollJob?.isActive == true) return
        scrollJob = coroutineScope.launch {
            snapshotFlow { scrollState.maxValue }.collect { scrollState.scrollTo(it) }
        }
    }

    fun update(scrollState: ScrollState) {
        if (this.scrollState === scrollState) return
        scrollJob?.cancel()
        scrollJob = null
        this.scrollState = scrollState
    }
}
