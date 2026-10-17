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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
 * @param onDismissRequest  뒤로가기, 딤 탭 시 호출
 * @param contentPadding    카드 안쪽 여백
 */
@Composable
internal fun RouteeDialogContainer(
    onDismissRequest: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
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

        // dim_rectangle
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(RouteeTheme.colors.dimSecondary)
                .pointerInput(onDismissRequest) { detectTapGestures { onDismissRequest() } },
            contentAlignment = Alignment.Center,
        ) {
            DialogCard(
                contentPadding = contentPadding,
                modifier = Modifier
                    .padding(horizontal = DialogHorizontalMargin)
                    .pointerInput(Unit) { detectTapGestures { } }, // 카드 안 터치가 딤 닫기로 가지 않게
                content = content,
            )
        }
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
