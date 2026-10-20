package com.routee.android.feature.recordedit.component.editor

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import com.routee.android.core.designsystem.R as DesignSystemR
import com.routee.android.core.designsystem.extension.noRippleClickable
import com.routee.android.core.designsystem.preview.PreviewFontSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.feature.recordedit.R

/**
 * 편집 초기화 버튼 (btn_reset_recap)
 *
 * @param enabled  편집한 내용이 있을 때 true. false 이면 회색 아이콘, 탭 불가
 * @param onClick  탭 시 호출
 */
@Composable
internal fun RecapResetButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(RouteeTheme.colors.black)
            .noRippleClickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(RouteeTheme.spacing.Spacing6),
    ) {
        Image(
            imageVector = ImageVector.vectorResource(
                if (enabled) DesignSystemR.drawable.ic_reset_sm_white else DesignSystemR.drawable.ic_reset_sm_grey,
            ),
            contentDescription = stringResource(R.string.record_edit_editor_reset_a11y),
        )
    }
}

@PreviewFontSizes
@Composable
private fun RecapResetButtonPreview() {
    RouteePreviewTheme {
        Row {
            RecapResetButton(enabled = true, onClick = {})
            RecapResetButton(enabled = false, onClick = {})
        }
    }
}
