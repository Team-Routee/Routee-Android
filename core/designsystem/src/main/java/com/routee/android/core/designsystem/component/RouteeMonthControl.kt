package com.routee.android.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import com.routee.android.core.designsystem.R
import com.routee.android.core.designsystem.extension.noRippleClickable
import com.routee.android.core.designsystem.preview.PreviewFontSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import java.time.YearMonth

// month_control

/**
 * 월 이동 컨트롤 크기
 */
enum class RouteeMonthControlSize {
    LG,
    SM,
}

/**
 * 월 이동 컨트롤
 *
 * @param yearMonth            보여줄 달
 * @param onPreviousClick      이전 달 버튼 탭 시 호출
 * @param onNextClick          다음 달 버튼 탭 시 호출
 * @param isPreviousEnabled    false 이면 이전 달 버튼 회색, 탭 무시
 * @param isNextEnabled        false 이면 다음 달 버튼 회색, 탭 무시
 */
@Composable
fun RouteeMonthControl(
    yearMonth: YearMonth,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    isPreviousEnabled: Boolean,
    isNextEnabled: Boolean,
    modifier: Modifier = Modifier,
    size: RouteeMonthControlSize = RouteeMonthControlSize.LG,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(size.spacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MonthChevronButton(
            enabledIconRes = R.drawable.ic_chevron_left_sm_white,
            disabledIconRes = R.drawable.ic_chevron_left_sm_grey,
            contentDescription = stringResource(R.string.designsystem_month_control_previous_a11y),
            enabled = isPreviousEnabled,
            onClick = onPreviousClick,
        )
        Text(
            text = stringResource(R.string.designsystem_month_control_year_month, yearMonth.year, yearMonth.monthValue),
            style = size.textStyle,
            color = RouteeTheme.colors.white,
        )
        MonthChevronButton(
            enabledIconRes = R.drawable.ic_chevron_right_sm_white,
            disabledIconRes = R.drawable.ic_chevron_right_sm_grey,
            contentDescription = stringResource(R.string.designsystem_month_control_next_a11y),
            enabled = isNextEnabled,
            onClick = onNextClick,
        )
    }
}

// btn_chevron_left, btn_chevron_right
@Composable
private fun MonthChevronButton(
    @DrawableRes enabledIconRes: Int,
    @DrawableRes disabledIconRes: Int,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Icon(
        imageVector = ImageVector.vectorResource(if (enabled) enabledIconRes else disabledIconRes),
        contentDescription = contentDescription,
        tint = Color.Unspecified,
        modifier = modifier
            .noRippleClickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(RouteeTheme.spacing.Spacing2),
    )
}

private val RouteeMonthControlSize.spacing: Dp
    @Composable
    @ReadOnlyComposable
    get() = when (this) {
        RouteeMonthControlSize.LG -> RouteeTheme.spacing.Spacing8
        RouteeMonthControlSize.SM -> RouteeTheme.spacing.Spacing6
    }

private val RouteeMonthControlSize.textStyle: TextStyle
    @Composable
    @ReadOnlyComposable
    get() = when (this) {
        RouteeMonthControlSize.LG -> RouteeTheme.typography.titleSb18
        RouteeMonthControlSize.SM -> RouteeTheme.typography.bodyR16
    }

@PreviewFontSizes
@Composable
private fun RouteeMonthControlPreview() {
    RouteePreviewTheme {
        Column(verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing16)) {
            RouteeMonthControl(
                yearMonth = YearMonth.of(2026, 3),
                onPreviousClick = {},
                onNextClick = {},
                isPreviousEnabled = true,
                isNextEnabled = false,
            )
            RouteeMonthControl(
                yearMonth = YearMonth.of(2026, 3),
                onPreviousClick = {},
                onNextClick = {},
                isPreviousEnabled = false,
                isNextEnabled = true,
                size = RouteeMonthControlSize.SM,
            )
        }
    }
}
