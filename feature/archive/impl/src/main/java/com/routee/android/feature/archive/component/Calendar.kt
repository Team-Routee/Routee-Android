package com.routee.android.feature.archive.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.component.RouteeAsyncImage
import com.routee.android.core.designsystem.extension.noRippleClickable
import com.routee.android.core.designsystem.preview.PreviewFontSizes
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.feature.archive.R

private const val DAYS_IN_WEEK = 7
private val DayCellSize = 36.dp
private val WeekdayLabelResources = intArrayOf(
    R.string.archive_calendar_weekday_monday,
    R.string.archive_calendar_weekday_tuesday,
    R.string.archive_calendar_weekday_wednesday,
    R.string.archive_calendar_weekday_thursday,
    R.string.archive_calendar_weekday_friday,
    R.string.archive_calendar_weekday_saturday,
    R.string.archive_calendar_weekday_sunday,
)

internal data class CalendarDay(
    val day: Int,
    val recordCount: Int = 0,
    val photoUrl: String? = null,
)

/**
 * 월요일부터 일요일 순서로 날짜를 표시하고, 비어 있는 날짜 칸은 빈 칸으로 표시한다.
 */
@Composable
internal fun Calendar(
    days: List<CalendarDay?>,
    modifier: Modifier = Modifier,
    onDayClick: ((CalendarDay) -> Unit)? = null,
) {
    val weekCount = (days.size + DAYS_IN_WEEK - 1) / DAYS_IN_WEEK

    Column(
        modifier = modifier.padding(horizontal = 11.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing20),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            WeekdayLabelResources.forEach { weekdayLabelResource ->
                Box(
                    modifier = Modifier.size(width = DayCellSize, height = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(weekdayLabelResource),
                        color = RouteeTheme.colors.grey400,
                        style = RouteeTheme.typography.labelM12,
                    )
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing16),
        ) {
            repeat(weekCount) { week ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    repeat(DAYS_IN_WEEK) { weekday ->
                        val day = days.getOrNull(week * DAYS_IN_WEEK + weekday)
                        if (day == null) {
                            Box(modifier = Modifier.size(DayCellSize))
                        } else {
                            CalendarDayCell(day = day, onDayClick = onDayClick)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: CalendarDay,
    onDayClick: ((CalendarDay) -> Unit)?,
) {
    val shape = RoundedCornerShape(RouteeTheme.radius.Radius12)
    val hasRecord = day.recordCount > 0
    val hasPhoto = !day.photoUrl.isNullOrBlank()
    val dayContentDescription = if (hasPhoto || hasRecord) {
        stringResource(R.string.archive_calendar_photo_day_a11y, day.day, day.recordCount)
    } else {
        null
    }
    val dayCellModifier = if (dayContentDescription == null) {
        Modifier.size(DayCellSize)
    } else {
        Modifier
            .size(DayCellSize)
            .semantics(mergeDescendants = true) { contentDescription = dayContentDescription }
    }

    Box(
        modifier = dayCellModifier.then(
            if (hasRecord && onDayClick != null) {
                Modifier.noRippleClickable(role = Role.Button) { onDayClick(day) }
            } else {
                Modifier
            },
        ),
        contentAlignment = Alignment.Center,
    ) {
        when {
            hasPhoto -> RouteeAsyncImage(
                url = day.photoUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(DayCellSize)
                    .clip(shape),
            )

            hasRecord -> Box(
                modifier = Modifier
                    .size(DayCellSize)
                    .background(RouteeTheme.colors.grey600, shape),
            )
        }

        if (!hasPhoto) {
            Text(
                text = day.day.toString(),
                color = RouteeTheme.colors.white,
                style = if (hasRecord) RouteeTheme.typography.labelSb14 else RouteeTheme.typography.labelM14,
            )
        }

        when {
            day.recordCount > 1 -> CountBadge(
                count = day.recordCount,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(
                        x = if (day.recordCount > 9) 5.dp else 3.dp,
                        y = (-5).dp,
                    ),
            )

            day.recordCount == 1 && !hasPhoto -> Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-8).dp, y = 8.dp)
                    .size(4.dp)
                    .background(RouteeTheme.colors.mint400, CircleShape),
            )
        }
    }
}

@PreviewFontSizes
@Composable
private fun CalendarPreview() {
    RouteeTheme {
        Calendar(
            days = List(3) { null } + (1..31).map { day ->
                CalendarDay(
                    day = day,
                    recordCount = when (day) {
                        20 -> 2
                        21 -> 12
                        30 -> 1
                        else -> 0
                    },
                    photoUrl = if (day == 12) "https://example.com/photo.jpg" else null,
                )
            } + listOf(null),
            modifier = Modifier.width(328.dp),
        )
    }
}
