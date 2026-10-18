package com.routee.android.feature.setting.component.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.R as DesignSystemR
import com.routee.android.core.designsystem.extension.noRippleClickable
import com.routee.android.core.designsystem.preview.PreviewFontSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeSpacing
import com.routee.android.core.designsystem.theme.RouteeTheme

private val SectionPadding = PaddingValues(
    start = RouteeSpacing.Spacing18,
    top = RouteeSpacing.Spacing18,
    end = RouteeSpacing.Spacing14,
    bottom = RouteeSpacing.Spacing10,
)
private val MenuItemMinHeight = 40.dp

/**
 * 설정 섹션 카드
 *
 * @param title    카드 위 작은 제목
 * @param content  [SettingMenuItem] 목록
 */
@Composable
internal fun SettingSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color = RouteeTheme.colors.grey900, shape = RoundedCornerShape(RouteeTheme.radius.Radius12))
            .padding(SectionPadding),
    ) {
        Text(
            text = title,
            style = RouteeTheme.typography.labelR12,
            color = RouteeTheme.colors.grey200,
        )
        Spacer(modifier = Modifier.height(RouteeTheme.spacing.Spacing10))
        content()
    }
}

/**
 * 설정 메뉴 항목
 *
 * @param onClick       탭 시 호출. null 이면 탭 불가, 화살표 숨김
 * @param trailingText  오른쪽 값 (예: 버전). null 이면 숨김
 */
@Composable
internal fun SettingMenuItem(
    title: String,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    trailingText: String? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = MenuItemMinHeight)
            .then(if (onClick != null) Modifier.noRippleClickable(role = Role.Button, onClick = onClick) else Modifier),
        horizontalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = RouteeTheme.typography.labelM16,
            color = RouteeTheme.colors.white,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (trailingText != null) {
            Text(
                text = trailingText,
                style = RouteeTheme.typography.labelR14,
                color = RouteeTheme.colors.white,
                maxLines = 1,
            )
        }
        if (onClick != null) {
            Icon(
                imageVector = ImageVector.vectorResource(DesignSystemR.drawable.ic_chevron_right_sm_white),
                contentDescription = null,
                tint = Color.Unspecified,
            )
        }
    }
}

@PreviewFontSizes
@Composable
private fun SettingSectionPreview() {
    RouteePreviewTheme(modifier = Modifier.padding(RouteeTheme.spacing.Spacing16)) {
        SettingSection(title = "앱 정보") {
            SettingMenuItem(title = "버전정보", onClick = null, trailingText = "1.0.0")
            SettingMenuItem(title = "로그아웃", onClick = {})
        }
    }
}
