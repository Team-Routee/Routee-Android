package com.routee.android.feature.recordedit.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.routee.android.core.designsystem.component.RouteeBgEllipse
import com.routee.android.core.designsystem.component.RouteeMonthControl
import com.routee.android.core.designsystem.component.RouteePageHeader
import com.routee.android.core.designsystem.component.routeeNavBarPadding
import com.routee.android.core.designsystem.extension.clearFocus
import com.routee.android.core.designsystem.preview.PreviewDeviceSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.core.ui.CollectSideEffect
import com.routee.android.core.ui.UiState
import com.routee.android.core.ui.error.RouteeErrorScreen
import com.routee.android.core.ui.error.RouteeErrorType
import com.routee.android.core.ui.loading.RouteeLoadingOverlay
import com.routee.android.feature.recordedit.R
import com.routee.android.feature.recordedit.component.main.RecordEditEmptyContent
import com.routee.android.feature.recordedit.component.main.WorkoutRecapItem
import com.routee.android.feature.recordedit.main.model.RecordEditMainSideEffect
import com.routee.android.feature.recordedit.main.model.RecordEditMainUiState
import com.routee.android.feature.recordedit.model.RecapItemUiModel
import java.time.YearMonth
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun RecordEditMainRoute(
    onNavigateToEditor: (activityId: Long) -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: RecordEditMainViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    viewModel.sideEffect.CollectSideEffect { sideEffect ->
        when (sideEffect) {
            is RecordEditMainSideEffect.NavigateToEditor -> onNavigateToEditor(sideEffect.activityId)
        }
    }

    RecordEditMainScreen(
        uiState = uiState,
        titleEditingState = viewModel.titleEditingState,
        onPreviousMonthClick = viewModel::onPreviousMonthClick,
        onNextMonthClick = viewModel::onNextMonthClick,
        onRecapClick = viewModel::onRecapClick,
        onEditTitleClick = viewModel::onEditTitleClick,
        onEditTitleComplete = viewModel::onEditTitleComplete,
        onRetryClick = viewModel::onRetryClick,
        onHomeClick = onNavigateToHome,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecordEditMainScreen(
    uiState: RecordEditMainUiState,
    titleEditingState: TextFieldState,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
    onRecapClick: (Long) -> Unit,
    onEditTitleClick: (Long) -> Unit,
    onEditTitleComplete: (Long) -> Unit,
    onRetryClick: () -> Unit,
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val recaps = uiState.recaps
    if (recaps is UiState.Failure) {
        RouteeErrorScreen(
            type = RouteeErrorType.Unknown,
            onHomeClick = onHomeClick,
            onRetryClick = onRetryClick,
            modifier = modifier,
        )
        return
    }

    val focusManager = LocalFocusManager.current
    val isImeVisible = WindowInsets.isImeVisible

    LaunchedEffect(isImeVisible) {
        if (!isImeVisible) focusManager.clearFocus()
    }

    RouteeBgEllipse(modifier = modifier.clearFocus(focusManager)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            RouteePageHeader(title = stringResource(R.string.record_edit_main_title))
            Spacer(modifier = Modifier.height(RouteeTheme.spacing.Spacing12))
            RouteeMonthControl(
                yearMonth = uiState.selectedMonth,
                onPreviousClick = onPreviousMonthClick,
                onNextClick = onNextMonthClick,
                isPreviousEnabled = uiState.isPreviousMonthEnabled,
                isNextEnabled = uiState.isNextMonthEnabled,
            )
            Spacer(modifier = Modifier.height(RouteeTheme.spacing.Spacing20))
            if (recaps is UiState.Success) {
                RecapGrid(
                    recaps = recaps.data,
                    editingActivityId = uiState.editingActivityId,
                    titleEditingState = titleEditingState,
                    onRecapClick = onRecapClick,
                    onEditTitleClick = onEditTitleClick,
                    onEditTitleComplete = onEditTitleComplete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .imePadding(),
                )
            }
        }
        if (recaps is UiState.Empty) {
            RecordEditEmptyContent(modifier = Modifier.align(Alignment.Center))
        }
    }

    if (recaps is UiState.Loading) RouteeLoadingOverlay()
}

@Composable
private fun RecapGrid(
    recaps: ImmutableList<RecapItemUiModel>,
    editingActivityId: Long?,
    titleEditingState: TextFieldState,
    onRecapClick: (Long) -> Unit,
    onEditTitleClick: (Long) -> Unit,
    onEditTitleComplete: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(RECAP_GRID_COLUMNS),
        modifier = modifier,
        contentPadding = PaddingValues(
            start = RouteeTheme.spacing.Spacing24,
            end = RouteeTheme.spacing.Spacing24,
            bottom = routeeNavBarPadding(RouteeTheme.spacing.Spacing20),
        ),
        horizontalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing24),
        verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing24),
    ) {
        items(items = recaps, key = { it.activityId }) { recap ->
            WorkoutRecapItem(
                item = recap,
                editingTitleState = titleEditingState.takeIf { recap.activityId == editingActivityId },
                onItemClick = { onRecapClick(recap.activityId) },
                onEditTitleClick = { onEditTitleClick(recap.activityId) },
                onEditTitleComplete = { onEditTitleComplete(recap.activityId) },
            )
        }
    }
}

private const val RECAP_GRID_COLUMNS = 2

private class RecordEditMainUiStatePreviewParameterProvider : PreviewParameterProvider<RecordEditMainUiState> {
    private val month = YearMonth.of(2026, 3)
    private val recaps = listOf(
        RecapItemUiModel(1L, "숭실대 동기모임 2탄", "2026.03.23.", photos(2)),
        RecapItemUiModel(2L, "2026.03.19 기록", "2026.03.19.", photos(4)),
        RecapItemUiModel(3L, "백석동천 저녁 러닝", "2026.03.14.", persistentListOf()),
        RecapItemUiModel(4L, "숭실대 동기모임 북악산", "2026.03.10.", photos(1)),
        RecapItemUiModel(5L, "2026.03.07 기록", "2026.03.07.", photos(3)),
    ).toImmutableList()

    override val values = sequenceOf(
        RecordEditMainUiState(selectedMonth = month, currentMonth = month, recaps = UiState.Success(recaps)),
        RecordEditMainUiState(
            selectedMonth = month,
            currentMonth = month,
            recaps = UiState.Success(recaps),
            editingActivityId = 1L,
        ),
        RecordEditMainUiState(selectedMonth = month, currentMonth = month, recaps = UiState.Empty),
    )

    private fun photos(count: Int) = List(count) { "https://example.com/photo_$it.jpg" }.toImmutableList()
}

@PreviewDeviceSizes
@Composable
private fun RecordEditMainScreenPreview(
    @PreviewParameter(RecordEditMainUiStatePreviewParameterProvider::class) uiState: RecordEditMainUiState,
) {
    RouteePreviewTheme {
        RecordEditMainScreen(
            uiState = uiState,
            titleEditingState = rememberTextFieldState("숭실대 동기모임 2탄"),
            onPreviousMonthClick = {},
            onNextMonthClick = {},
            onRecapClick = {},
            onEditTitleClick = {},
            onEditTitleComplete = {},
            onRetryClick = {},
            onHomeClick = {},
        )
    }
}
