package com.routee.android.core.designsystem.component.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.routee.android.core.designsystem.extension.noRippleClickable
import com.routee.android.core.designsystem.theme.RouteeTheme

private val DialogHorizontalMargin = 28.dp
private val DialogMaxWidth = 304.dp

/**
 * 다이얼로그 공통 틀. 화면 전체 딤 + 가운데 카드
 *
 * 시스템 바와 키보드를 피해 가운데에 놓인다. 키보드가 올라와 공간이 모자라면 카드 크기는 그대로 두고 위쪽을 화면 밖으로 밀어낸다
 *
 * @param onDismissRequest   뒤로가기, 딤 탭 시 호출
 * @param contentPadding     카드 안쪽 여백
 * @param dismissOnDimClick  false 이면 딤 탭 시 닫지 않고 키보드만 닫음
 */
@Composable
fun RouteeDialogContainer(
    onDismissRequest: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    dismissOnDimClick: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        val window = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect { window?.setDimAmount(0f) }
        val focusManager = LocalFocusManager.current

        // dim_rectangle
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(RouteeTheme.colors.dimSecondary)
                .pointerInput(onDismissRequest, dismissOnDimClick) {
                    detectTapGestures { if (dismissOnDimClick) onDismissRequest() else focusManager.clearFocus() }
                }
                .safeDrawingPadding(),
            contentAlignment = Alignment.Center,
        ) {
            DialogCard(
                contentPadding = contentPadding,
                modifier = Modifier
                    .overflowTop()
                    .padding(horizontal = DialogHorizontalMargin)
                    .pointerInput(Unit) { detectTapGestures { } }, // 카드 안 터치가 딤 닫기로 가지 않게
                content = content,
            )
        }
    }
}

// 공간보다 크면 아래(키보드 쪽)를 맞추고 위를 잘라낸다
private fun Modifier.overflowTop(): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints.copy(maxHeight = Constraints.Infinity))
    val height = minOf(placeable.height, constraints.maxHeight)
    layout(placeable.width, height) {
        placeable.place(0, height - placeable.height)
    }
}

/**
 * 다이얼로그 카드
 */
@Composable
internal fun DialogCard(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .widthIn(max = DialogMaxWidth)
            .fillMaxWidth()
            .background(color = RouteeTheme.colors.grey800, shape = RoundedCornerShape(RouteeTheme.radius.Radius24))
            .padding(contentPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content,
    )
}

// btn_action_in_modal
@Composable
internal fun DialogActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false,
) {
    Text(
        text = text,
        style = RouteeTheme.typography.labelSb16,
        color = if (isDestructive) RouteeTheme.colors.statusError else RouteeTheme.colors.white,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .background(color = RouteeTheme.colors.white10, shape = RoundedCornerShape(RouteeTheme.radius.Full))
            .noRippleClickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = RouteeTheme.spacing.Spacing16, vertical = RouteeTheme.spacing.Spacing12),
    )
}
