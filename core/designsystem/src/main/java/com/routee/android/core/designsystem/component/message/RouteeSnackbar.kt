package com.routee.android.core.designsystem.component.message

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.extension.noRippleClickable
import com.routee.android.core.designsystem.preview.PreviewFontSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme

// snackbar_android
private val SnackbarMinHeight = 46.dp

/**
 * 스낵바
 *
 * [actionLabel]이 있으면 오른쪽에 버튼을 보여준다
 *
 * 띄우기는 [RouteeMessageState]로 한다
 */
@Composable
fun RouteeSnackbar(
    text: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .heightIn(min = SnackbarMinHeight)
            .background(
                color = RouteeTheme.colors.grey900,
                shape = RoundedCornerShape(RouteeTheme.radius.Radius12),
            )
            .padding(start = RouteeTheme.spacing.Spacing16, end = RouteeTheme.spacing.Spacing6),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = RouteeTheme.typography.labelR12,
            color = RouteeTheme.colors.white,
            modifier = Modifier.weight(1f),
        )
        if (actionLabel != null) {
            // btn_in_snackbar
            Text(
                text = actionLabel,
                style = RouteeTheme.typography.labelSb14,
                color = RouteeTheme.colors.statusError,
                modifier = Modifier
                    .noRippleClickable(role = Role.Button, onClick = onActionClick)
                    .padding(horizontal = RouteeTheme.spacing.Spacing12, vertical = RouteeTheme.spacing.Spacing8),
            )
        }
    }
}

@PreviewFontSizes
@Composable
private fun RouteeSnackbarPreview() {
    RouteePreviewTheme(modifier = Modifier.padding(16.dp)) {
        RouteeSnackbar(
            text = "삭제되었습니다",
            actionLabel = "취소",
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
