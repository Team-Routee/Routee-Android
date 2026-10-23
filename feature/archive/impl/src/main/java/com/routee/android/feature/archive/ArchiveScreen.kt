package com.routee.android.feature.archive

import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.routee.android.core.data.archive.model.DailyActivitySummary
import com.routee.android.core.designsystem.component.RouteeBgEllipse
import com.routee.android.core.designsystem.component.RouteeMonthControl
import com.routee.android.core.designsystem.component.RouteeMonthControlSize
import com.routee.android.core.designsystem.component.RouteePageHeader
import com.routee.android.core.designsystem.component.message.LocalRouteeMessageState
import com.routee.android.core.designsystem.component.message.RouteeMessageDuration
import com.routee.android.core.designsystem.component.routeeNavBarPadding
import com.routee.android.core.designsystem.preview.PreviewDeviceSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.core.ui.CollectSideEffect
import com.routee.android.core.ui.UiState
import com.routee.android.core.ui.error.RouteeErrorScreen
import com.routee.android.core.ui.error.RouteeErrorType
import com.routee.android.core.ui.loading.RouteeLoadingOverlay
import com.routee.android.feature.archive.component.ArchiveCalendarSection
import com.routee.android.feature.archive.component.MountainMap
import com.routee.android.feature.archive.component.UserProfile
import com.routee.android.feature.archive.model.ArchiveProfile
import com.routee.android.feature.archive.model.ArchiveSideEffect
import com.routee.android.feature.archive.model.ArchiveUiState
import com.routee.android.feature.archive.model.archiveContent
import java.time.YearMonth

@Composable
internal fun ArchiveRoute(
    onRecordClick: (Long) -> Unit,
    onHomeClick: () -> Unit,
    viewModel: ArchiveViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val messages = LocalRouteeMessageState.current
    val networkMessage = stringResource(R.string.archive_network_unavailable)
    viewModel.sideEffect.CollectSideEffect { effect ->
        when (effect) {
            ArchiveSideEffect.NetworkUnavailable -> messages.showToast(networkMessage, RouteeMessageDuration.LONG)
        }
    }
    ArchiveScreen(
        uiState = uiState,
        onPreviousMonthClick = viewModel::onPreviousMonthClick,
        onNextMonthClick = viewModel::onNextMonthClick,
        onRetryClick = viewModel::onRetryClick,
        onOfflineClick = viewModel::onRetryClick,
        onRecordClick = onRecordClick,
        onHomeClick = onHomeClick,
    )
}

@Composable
private fun ArchiveScreen(
    uiState: ArchiveUiState,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
    onRetryClick: () -> Unit,
    onOfflineClick: () -> Unit,
    onRecordClick: (Long) -> Unit,
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (val content = uiState.content) {
        is UiState.Failure, UiState.Empty -> RouteeErrorScreen(
            type = if (content == UiState.Empty) RouteeErrorType.NotFound else RouteeErrorType.Unknown,
            onHomeClick = onHomeClick,
            onRetryClick = onRetryClick,
            modifier = modifier.fillMaxSize(),
        )

        UiState.Loading -> RouteeBgEllipse(modifier = modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize()) { RouteeLoadingOverlay() }
        }

        is UiState.Success -> RouteeBgEllipse(modifier = modifier.fillMaxSize()) {
            val data = content.data
            Column(
                modifier = Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                RouteePageHeader(title = stringResource(R.string.archive_title))
                UserProfile(
                    nickname = data.profile.nickname,
                    daysTogether = data.profile.daysTogether,
                    workoutCount = data.profile.workoutCount,
                    profileImageUrl = data.profile.imageUrl,
                    modifier = Modifier.padding(horizontal = RouteeTheme.spacing.Spacing16),
                )
                Spacer(Modifier.height(22.dp))
                RouteeMonthControl(
                    yearMonth = uiState.selectedMonth,
                    onPreviousClick = onPreviousMonthClick,
                    onNextClick = onNextMonthClick,
                    isPreviousEnabled = uiState.isPreviousMonthEnabled,
                    isNextEnabled = uiState.isNextMonthEnabled,
                    size = RouteeMonthControlSize.SM,
                )
                Spacer(Modifier.height(RouteeTheme.spacing.Spacing6))
                MountainMap(
                    activityLevels = data.activityLevels,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = RouteeTheme.spacing.Spacing16),
                )
                Spacer(Modifier.height(RouteeTheme.spacing.Spacing32))
                ArchiveCalendarSection(
                    month = data.month,
                    days = data.calendarDays,
                    records = emptyList(),
                    onRecordClick = onRecordClick,
                    onDateClick = if (uiState.isOffline) {
                        { onOfflineClick() }
                    } else {
                        null
                    },
                    modifier = Modifier.padding(horizontal = RouteeTheme.spacing.Spacing16),
                )
                Spacer(Modifier.height(routeeNavBarPadding(RouteeTheme.spacing.Spacing16)))
            }
            if (uiState.isRefreshing) RouteeLoadingOverlay()
        }
    }
}

@PreviewDeviceSizes
@Composable
private fun ArchiveScreenPreview() {
    RouteePreviewTheme {
        ArchiveScreen(
            uiState = archivePreviewState(),
            onPreviousMonthClick = {},
            onNextMonthClick = {},
            onRetryClick = {},
            onOfflineClick = {},
            onRecordClick = {},
            onHomeClick = {},
        )
    }
}

@PreviewDeviceSizes
@Composable
private fun ArchiveScreenOfflinePreview() {
    RouteePreviewTheme {
        ArchiveScreen(
            uiState = archivePreviewState().copy(isOffline = true),
            onPreviousMonthClick = {},
            onNextMonthClick = {},
            onRetryClick = {},
            onOfflineClick = {},
            onRecordClick = {},
            onHomeClick = {},
        )
    }
}

@PreviewDeviceSizes
@Composable
private fun ArchiveScreenEmptyPreview() {
    RouteePreviewTheme {
        ArchiveScreen(
            uiState = archivePreviewState(hasRecords = false),
            onPreviousMonthClick = {},
            onNextMonthClick = {},
            onRetryClick = {},
            onOfflineClick = {},
            onRecordClick = {},
            onHomeClick = {},
        )
    }
}

private fun archivePreviewState(hasRecords: Boolean = true): ArchiveUiState {
    val month = YearMonth.of(2026, 3)
    return ArchiveUiState(
        currentMonth = month,
        selectedMonth = month,
        content = UiState.Success(
            archiveContent(
                profile = ArchiveProfile("관악산 날다람쥐", 8, if (hasRecords) 25 else 0, month, null),
                month = month,
                summaries = if (hasRecords) {
                    listOf(
                        DailyActivitySummary(month.atDay(2), 30, 1, null),
                        DailyActivitySummary(month.atDay(8), 90, 1, null),
                        DailyActivitySummary(month.atDay(15), 130, 1, null),
                        DailyActivitySummary(month.atDay(22), 190, 12, null),
                    )
                } else {
                    emptyList()
                },
            ),
        ),
    )
}
