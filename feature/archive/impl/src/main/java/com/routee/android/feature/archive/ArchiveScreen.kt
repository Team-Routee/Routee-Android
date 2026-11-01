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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.result.ResultEffect
import com.routee.android.core.data.archive.model.DailyActivitySummary
import com.routee.android.core.designsystem.component.RouteeBgEllipse
import com.routee.android.core.designsystem.component.RouteeMonthControl
import com.routee.android.core.designsystem.component.RouteeMonthControlSize
import com.routee.android.core.designsystem.component.RouteePageHeader
import com.routee.android.core.designsystem.component.message.LocalRouteeMessageState
import com.routee.android.core.designsystem.component.message.RouteeMessage
import com.routee.android.core.designsystem.component.message.RouteeMessageDuration
import com.routee.android.core.designsystem.component.message.rememberRouteeMessageState
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
import com.routee.android.feature.archive.component.RecordBottomSheet
import com.routee.android.feature.archive.component.UserProfile
import com.routee.android.feature.archive.detail.model.ArchiveRecordChanged
import com.routee.android.feature.archive.model.ArchiveProfile
import com.routee.android.feature.archive.model.ArchiveSideEffect
import com.routee.android.feature.archive.model.ArchiveUiState
import com.routee.android.feature.archive.model.archiveContent
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

private val SheetDateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

@Composable
internal fun ArchiveRoute(
    onRecordClick: (Long) -> Unit,
    onHomeClick: () -> Unit,
    viewModel: ArchiveViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ResultEffect<ArchiveRecordChanged> { viewModel.onRecordChanged() }
    val messages = LocalRouteeMessageState.current
    val sheetMessages = rememberRouteeMessageState()
    val isSheetVisible = uiState.selectedDate != null && uiState.records.size > 1
    LaunchedEffect(isSheetVisible) {
        if (!isSheetVisible) sheetMessages.dismiss()
    }
    val networkMessage = stringResource(R.string.archive_network_unavailable)
    val loadFailedMessage = stringResource(R.string.archive_record_load_failed)
    viewModel.sideEffect.CollectSideEffect { effect ->
        val state = viewModel.uiState.value
        val target = if (state.selectedDate != null && state.records.size > 1) sheetMessages else messages
        when (effect) {
            ArchiveSideEffect.NetworkUnavailable -> target.showToast(networkMessage, RouteeMessageDuration.LONG)
            ArchiveSideEffect.RecordLoadFailed -> target.showToast(loadFailedMessage)
            is ArchiveSideEffect.NavigateToDetail -> onRecordClick(effect.activityId)
        }
    }
    ArchiveScreen(
        uiState = uiState,
        recordMessage = (sheetMessages.currentMessage as? RouteeMessage.Toast)?.text,
        onPreviousMonthClick = viewModel::onPreviousMonthClick,
        onNextMonthClick = viewModel::onNextMonthClick,
        onRetryClick = viewModel::onRetryClick,
        onDateClick = viewModel::onDateClick,
        onRecordSheetDismiss = viewModel::onRecordSheetDismiss,
        onRecordClick = viewModel::onRecordClick,
        onHomeClick = onHomeClick,
    )
}

@Composable
private fun ArchiveScreen(
    uiState: ArchiveUiState,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
    onRetryClick: () -> Unit,
    onDateClick: (LocalDate) -> Unit,
    onRecordSheetDismiss: () -> Unit,
    onRecordClick: (Long) -> Unit,
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier,
    recordMessage: String? = null,
) {
    when (val content = uiState.content) {
        is UiState.Failure -> RouteeErrorScreen(
            type = RouteeErrorType.Unknown,
            onHomeClick = onHomeClick,
            onRetryClick = onRetryClick,
            modifier = modifier.fillMaxSize(),
        )

        UiState.Loading -> RouteeBgEllipse(modifier = modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize().statusBarsPadding()) {
                RouteePageHeader(title = stringResource(R.string.archive_title))
                RouteeLoadingOverlay()
            }
        }

        UiState.Empty -> RouteeBgEllipse(modifier = modifier.fillMaxSize()) {
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = onRetryClick,
                modifier = Modifier.fillMaxSize().statusBarsPadding(),
            ) {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    RouteePageHeader(title = stringResource(R.string.archive_title))
                }
            }
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
                    onDateClick = onDateClick,
                    modifier = Modifier.padding(horizontal = RouteeTheme.spacing.Spacing16),
                )
                Spacer(Modifier.height(routeeNavBarPadding(RouteeTheme.spacing.Spacing16)))
            }
            if (uiState.isRefreshing || uiState.isLoadingRecords) RouteeLoadingOverlay()
            val date = uiState.selectedDate
            if (date != null && uiState.records.size > 1) {
                RecordBottomSheet(
                    date = date.format(SheetDateFormatter),
                    records = uiState.records,
                    message = recordMessage,
                    onRecordClick = onRecordClick,
                    onDismissRequest = onRecordSheetDismiss,
                )
            }
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
            onDateClick = {},
            onRecordSheetDismiss = {},
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
            onDateClick = {},
            onRecordSheetDismiss = {},
            onRecordClick = {},
            onHomeClick = {},
        )
    }
}

@PreviewDeviceSizes
@Composable
private fun ArchiveScreenInitialOfflinePreview() {
    RouteePreviewTheme {
        ArchiveScreen(
            uiState = ArchiveUiState(content = UiState.Empty, isOffline = true),
            onPreviousMonthClick = {},
            onNextMonthClick = {},
            onRetryClick = {},
            onDateClick = {},
            onRecordSheetDismiss = {},
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
            onDateClick = {},
            onRecordSheetDismiss = {},
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
