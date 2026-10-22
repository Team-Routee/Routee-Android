package com.routee.android.feature.archive

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.component.RouteeBgEllipse
import com.routee.android.core.designsystem.component.RouteeMonthControl
import com.routee.android.core.designsystem.component.RouteeMonthControlSize
import com.routee.android.core.designsystem.component.RouteePageHeader
import com.routee.android.core.designsystem.component.routeeNavBarPadding
import com.routee.android.core.designsystem.preview.PreviewDeviceSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.feature.archive.component.ArchiveCalendarRecord
import com.routee.android.feature.archive.component.ArchiveCalendarSection
import com.routee.android.feature.archive.component.CalendarDay
import com.routee.android.feature.archive.component.MountainActivityLevel
import com.routee.android.feature.archive.component.MountainMap
import com.routee.android.feature.archive.component.UserProfile
import java.time.YearMonth

private data class ArchiveProfile(
    val nickname: String,
    val daysTogether: Int,
    val workoutCount: Int,
    val joinedMonth: YearMonth,
    val imageUrl: String? = null,
)

@Composable
internal fun ArchiveRoute(
    onRecordClick: (Long) -> Unit,
    calendarDays: List<CalendarDay> = emptyList(),
    records: List<ArchiveCalendarRecord> = emptyList(),
) {
    val currentMonth = remember { YearMonth.now() }
    var monthOffset by rememberSaveable { mutableIntStateOf(0) }
    val selectedMonth = currentMonth.plusMonths(monthOffset.toLong())
    val profile = remember(currentMonth) {
        ArchiveProfile(
            nickname = "관악산 날다람쥐",
            daysTogether = 8,
            workoutCount = 25,
            joinedMonth = currentMonth.minusMonths(2),
        )
    }

    ArchiveScreen(
        profile = profile,
        selectedMonth = selectedMonth,
        currentMonth = currentMonth,
        activityLevels = remember { List(33) { MountainActivityLevel.NONE } },
        onPreviousMonthClick = { monthOffset-- },
        onNextMonthClick = { monthOffset++ },
        calendarDays = calendarDays,
        records = records,
        onRecordClick = onRecordClick,
    )
}

@Composable
private fun ArchiveScreen(
    profile: ArchiveProfile,
    selectedMonth: YearMonth,
    currentMonth: YearMonth,
    activityLevels: List<MountainActivityLevel>,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
    modifier: Modifier = Modifier,
    calendarDays: List<CalendarDay> = emptyList(),
    records: List<ArchiveCalendarRecord> = emptyList(),
    onRecordClick: (Long) -> Unit = {},
) {
    RouteeBgEllipse(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            RouteePageHeader(title = stringResource(R.string.archive_title))
            UserProfile(
                nickname = profile.nickname,
                daysTogether = profile.daysTogether,
                workoutCount = profile.workoutCount,
                profileImageUrl = profile.imageUrl,
                modifier = Modifier.padding(horizontal = RouteeTheme.spacing.Spacing16),
            )
            Spacer(Modifier.height(22.dp))
            RouteeMonthControl(
                yearMonth = selectedMonth,
                onPreviousClick = onPreviousMonthClick,
                onNextClick = onNextMonthClick,
                isPreviousEnabled = selectedMonth > profile.joinedMonth,
                isNextEnabled = selectedMonth < currentMonth,
                size = RouteeMonthControlSize.SM,
            )
            Spacer(Modifier.height(RouteeTheme.spacing.Spacing6))
            MountainMap(
                activityLevels = activityLevels,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = RouteeTheme.spacing.Spacing16),
            )
            Spacer(Modifier.height(RouteeTheme.spacing.Spacing32))
            ArchiveCalendarSection(
                month = selectedMonth,
                days = calendarDays,
                records = records,
                onRecordClick = onRecordClick,
                modifier = Modifier.padding(horizontal = RouteeTheme.spacing.Spacing16),
            )
            Spacer(Modifier.height(routeeNavBarPadding(RouteeTheme.spacing.Spacing16)))
        }
    }
}

@PreviewDeviceSizes
@Composable
private fun ArchiveScreenPreview() {
    RouteePreviewTheme {
        ArchiveScreen(
            profile = ArchiveProfile("관악산 날다람쥐", 8, 25, YearMonth.of(2026, 1)),
            selectedMonth = YearMonth.of(2026, 3),
            currentMonth = YearMonth.of(2026, 3),
            activityLevels = List(33) { index -> MountainActivityLevel.entries[index % 5] },
            onPreviousMonthClick = {},
            onNextMonthClick = {},
        )
    }
}

@PreviewDeviceSizes
@Composable
private fun ArchiveScreenEmptyPreview() {
    RouteePreviewTheme {
        ArchiveScreen(
            profile = ArchiveProfile("관악산 날다람쥐", 8, 0, YearMonth.of(2026, 3)),
            selectedMonth = YearMonth.of(2026, 3),
            currentMonth = YearMonth.of(2026, 3),
            activityLevels = List(33) { MountainActivityLevel.NONE },
            onPreviousMonthClick = {},
            onNextMonthClick = {},
        )
    }
}
