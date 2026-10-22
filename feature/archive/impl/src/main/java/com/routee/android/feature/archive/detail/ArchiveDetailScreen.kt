package com.routee.android.feature.archive.detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.routee.android.core.common.util.suspendRunCatching
import com.routee.android.core.data.archive.model.ArchivePhoto
import java.time.Instant
import com.routee.android.core.designsystem.component.RouteeBgEllipse
import com.routee.android.core.designsystem.component.RouteePageNavBar
import com.routee.android.core.designsystem.component.RouteePhotoTimelineSection
import com.routee.android.core.designsystem.component.RouteeTimelinePhoto
import com.routee.android.core.designsystem.component.RouteeWorkoutMetricCard
import com.routee.android.core.designsystem.component.dialog.RouteeTwoButtonDialog
import com.routee.android.core.designsystem.component.message.LocalRouteeMessageState
import com.routee.android.core.designsystem.component.textfield.RouteeRecordSummaryTextField
import com.routee.android.core.designsystem.extension.clearFocus
import com.routee.android.core.designsystem.extension.noRippleClickable
import com.routee.android.core.designsystem.preview.PreviewDeviceSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.core.map.RouteePhotoRouteMap
import com.routee.android.core.network.exception.ApiException
import com.routee.android.core.ui.UiState
import com.routee.android.core.ui.error.RouteeErrorScreen
import com.routee.android.core.ui.error.RouteeErrorType
import com.routee.android.core.ui.loading.LocalRouteeLoadingState
import com.routee.android.core.ui.loading.RouteeLoadingHost
import com.routee.android.core.ui.loading.RouteeLoadingOverlay
import com.routee.android.core.ui.loading.RouteeLoadingState
import com.routee.android.core.ui.photo.rememberGalleryPhotoSaveState
import com.routee.android.feature.archive.R
import com.routee.android.feature.archive.component.RoutePointItem
import com.routee.android.feature.archive.detail.model.ArchiveDetailRecord
import com.routee.android.feature.archive.detail.model.ArchiveDetailUiState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch

@Composable
internal fun ArchiveDetailRoute(
    viewModel: ArchiveDetailViewModel,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onSaveTitle: suspend (String) -> Boolean = { false },
    onDeleteRecord: suspend () -> Boolean = { false },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var expanded by rememberSaveable { mutableStateOf(false) }
    val record = (uiState.record as? UiState.Success)?.data
    val titleState = rememberTextFieldState()
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val messageState = LocalRouteeMessageState.current
    val gallerySave = rememberGalleryPhotoSaveState(viewModel::savePhoto)
    var isSaving by remember { mutableStateOf(false) }
    val defaultTitle = stringResource(R.string.archive_detail_default_title, record?.date.orEmpty())
    val updateFailed = stringResource(R.string.archive_detail_update_failed)
    val deleteFailed = stringResource(R.string.archive_detail_delete_failed)
    val deleted = stringResource(R.string.archive_detail_deleted)
    LaunchedEffect(record?.title) {
        if (!viewModel.uiState.value.isTitleEditing) {
            titleState.setTextAndPlaceCursorAtEnd(record?.title.orEmpty())
        }
    }

    fun finishTitleEdit(onFinished: () -> Unit = {}) {
        if (isSaving) return
        if (!viewModel.uiState.value.isTitleEditing || record == null) {
            onFinished()
            return
        }
        viewModel.onTitleEditingChange(false)
        focusManager.clearFocus()
        val title = titleState.text.toString().trim().ifEmpty { defaultTitle }
        titleState.setTextAndPlaceCursorAtEnd(title)
        if (title == record.title) {
            onFinished()
            return
        }
        isSaving = true
        scope.launch {
            try {
                if (!suspendRunCatching { onSaveTitle(title) }.getOrDefault(false)) {
                    titleState.setTextAndPlaceCursorAtEnd(record.title)
                    messageState.showToast(updateFailed)
                } else {
                    onFinished()
                }
            } finally {
                isSaving = false
            }
        }
    }

    BackHandler(enabled = isSaving || uiState.isTitleEditing) {
        if (!isSaving) finishTitleEdit()
    }
    ArchiveDetailScreen(
        uiState = uiState,
        expanded = expanded,
        onExpandedChange = { expanded = it },
        onBackClick = { finishTitleEdit(onBackClick) },
        titleState = titleState,
        savedPhotoIndex = gallerySave.savedPhotoIndex,
        onPhotoSaveClick = { index ->
            record?.photos?.getOrNull(index)?.let { photo ->
                gallerySave.onSavePhoto(index, photo.imageUrl, photo.createdAt.toEpochMilli())
            }
        },
        onEditTitleClick = { if (!isSaving) viewModel.onTitleEditingChange(true) },
        onTitleEditFinish = { finishTitleEdit() },
        onDeleteClick = { finishTitleEdit { viewModel.onDeleteDialogChange(true) } },
        onHomeClick = onHomeClick,
        onRetryClick = viewModel::onRetryClick,
    )
    if (isSaving) RouteeLoadingOverlay()
    if (uiState.isDeleteDialogVisible) {
        RouteeTwoButtonDialog(
            title = stringResource(R.string.archive_detail_delete_title),
            description = stringResource(R.string.archive_detail_delete_description),
            leftButtonText = stringResource(R.string.archive_detail_cancel),
            rightButtonText = stringResource(R.string.archive_detail_delete_confirm),
            onDismissRequest = { if (!isSaving) viewModel.onDeleteDialogChange(false) },
            onRightButtonClick = {
                if (!isSaving) {
                    isSaving = true
                    scope.launch {
                        try {
                            if (suspendRunCatching { onDeleteRecord() }.getOrDefault(false)) {
                                viewModel.onDeleteDialogChange(false)
                                messageState.showToast(deleted)
                                onBackClick()
                            } else {
                                messageState.showToast(deleteFailed)
                            }
                        } finally {
                            isSaving = false
                        }
                    }
                }
            },
        )
    }
}

@Composable
private fun ArchiveDetailScreen(
    uiState: ArchiveDetailUiState,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onHomeClick: () -> Unit = onBackClick,
    onRetryClick: () -> Unit = {},
    titleState: TextFieldState? = null,
    onEditTitleClick: () -> Unit = {},
    onTitleEditFinish: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    savedPhotoIndex: Int = -1,
    onPhotoSaveClick: (Int) -> Unit = {},
) {
    when (val record = uiState.record) {
        UiState.Empty, is UiState.Failure -> RouteeErrorScreen(
            type = if (record == UiState.Empty ||
                (record is UiState.Failure && record.throwable is ApiException.NotFound)
            ) {
                RouteeErrorType.NotFound
            } else {
                RouteeErrorType.Unknown
            },
            onHomeClick = onHomeClick,
            onRetryClick = onRetryClick,
            modifier = modifier.fillMaxSize().navigationBarsPadding(),
        )

        else -> RouteeBgEllipse(modifier = modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
                RouteePageNavBar(onBackClick = onBackClick, actions = {
                    if (record is UiState.Success) {
                        Text(
                            text = stringResource(R.string.archive_detail_delete),
                            style = RouteeTheme.typography.labelSb18,
                            color = RouteeTheme.colors.statusError,
                            modifier = Modifier.noRippleClickable(role = Role.Button, onClick = onDeleteClick)
                                .padding(RouteeTheme.spacing.Spacing16),
                        )
                    }
                })
                if (record is UiState.Success) {
                    ArchiveDetailContent(
                        record = record.data,
                        expanded = expanded,
                        onExpandedChange = onExpandedChange,
                        titleState = titleState ?: rememberTextFieldState(record.data.title),
                        savedPhotoIndex = savedPhotoIndex,
                        onPhotoSaveClick = onPhotoSaveClick,
                        isTitleEditing = uiState.isTitleEditing,
                        onEditTitleClick = onEditTitleClick,
                        onTitleEditFinish = onTitleEditFinish,
                        modifier = Modifier.weight(1f).imePadding(),
                    )
                } else {
                    RouteeLoadingOverlay()
                }
            }
        }
    }
}

@Composable
private fun ArchiveDetailContent(
    record: ArchiveDetailRecord,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    titleState: TextFieldState,
    isTitleEditing: Boolean,
    onEditTitleClick: () -> Unit,
    onTitleEditFinish: () -> Unit,
    savedPhotoIndex: Int,
    onPhotoSaveClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    Column(modifier = modifier.verticalScroll(rememberScrollState()).clearFocus(focusManager, onTitleEditFinish)) {
        Column(
            modifier = Modifier.padding(horizontal = RouteeTheme.spacing.Spacing16),
            verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing8),
        ) {
            RouteeRecordSummaryTextField(
                state = titleState,
                defaultTitle = record.title,
                isEditing = isTitleEditing,
                onEditClick = onEditTitleClick,
                onEditComplete = onTitleEditFinish,
            )
            RouteeWorkoutMetricCard(
                distance = record.distance,
                duration = record.duration,
                maxElevation = record.maxElevation,
                modifier = Modifier.fillMaxWidth(),
            )
            RouteePhotoRouteMap(route = record.track, photos = record.markers)
        }
        if (record.photos.isNotEmpty()) {
            Spacer(Modifier.height(52.dp))
            RouteePhotoTimelineSection(
                photos = remember(record.photos) {
                    record.photos.map { RouteeTimelinePhoto(it.imageUrl, it.location) }
                },
                date = record.date,
                savedPhotoIndex = savedPhotoIndex,
                onSaveClick = onPhotoSaveClick,
            )
        }
        if (record.routeNames.isNotEmpty()) {
            Spacer(Modifier.height(if (record.photos.isEmpty()) 52.dp else 48.dp))
            RoutePointItem(
                points = record.routeNames,
                expanded = expanded,
                onExpandedChange = onExpandedChange,
                modifier = Modifier.padding(horizontal = RouteeTheme.spacing.Spacing16),
            )
        } else if (record.photos.isEmpty()) {
            Spacer(Modifier.height(22.dp))
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = RouteeTheme.spacing.Spacing10),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.archive_detail_route_empty),
                    style = RouteeTheme.typography.labelM14,
                    color = RouteeTheme.colors.grey200,
                )
            }
        }
        Spacer(Modifier.height(if (record.routeNames.isEmpty()) 36.dp else 100.dp))
    }
}

@PreviewDeviceSizes
@Composable
private fun ArchiveDetailScreenPreview() {
    RouteePreviewTheme {
        var expanded by rememberSaveable { mutableStateOf(false) }
        ArchiveDetailScreen(
            uiState = ArchiveDetailUiState(
                UiState.Success(
                    ArchiveDetailRecord(
                        title = "숭실대 동기모임 북악산",
                        date = "2026.03.12",
                        distance = "15.53",
                        duration = "03:20",
                        maxElevation = "2132",
                        photos = persistentListOf(
                            ArchivePhoto(1L, "창의문", "", Instant.EPOCH),
                            ArchivePhoto(2L, null, "", Instant.EPOCH),
                        ),
                        routeNames = persistentListOf("창의문", "청운대", "말바위", "백숙 먹으러 가는 중"),
                    ),
                ),
            ),
            expanded = expanded,
            onExpandedChange = { expanded = it },
            onBackClick = {},
        )
    }
}

@PreviewDeviceSizes
@Composable
private fun ArchiveDetailScreenEmptyPreview() {
    RouteePreviewTheme {
        ArchiveDetailScreen(
            uiState = ArchiveDetailUiState(
                UiState.Success(ArchiveDetailRecord("북악산 산행", "2026.03.12", "15.53", "03:20", "2132")),
            ),
            expanded = false,
            onExpandedChange = {},
            onBackClick = {},
        )
    }
}

@PreviewDeviceSizes
@Composable
private fun ArchiveDetailScreenWithoutPhotosPreview() {
    RouteePreviewTheme {
        ArchiveDetailScreen(
            uiState = ArchiveDetailUiState(
                UiState.Success(
                    ArchiveDetailRecord(
                        title = "북악산 산행",
                        date = "2026.03.12",
                        distance = "15.53",
                        duration = "03:20",
                        maxElevation = "2132",
                        routeNames = persistentListOf("창의문", "청운대", "말바위"),
                    ),
                ),
            ),
            expanded = false,
            onExpandedChange = {},
            onBackClick = {},
        )
    }
}

@PreviewDeviceSizes
@Composable
private fun ArchiveDetailScreenFailurePreview() {
    RouteePreviewTheme {
        ArchiveDetailScreen(
            uiState = ArchiveDetailUiState(UiState.Failure(IllegalStateException())),
            expanded = false,
            onExpandedChange = {},
            onBackClick = {},
        )
    }
}

@PreviewDeviceSizes
@Composable
private fun ArchiveDetailScreenWithoutRoutesPreview() {
    RouteePreviewTheme {
        ArchiveDetailScreen(
            uiState = ArchiveDetailUiState(
                UiState.Success(
                    ArchiveDetailRecord(
                        title = "북악산 산행",
                        date = "2026.03.12",
                        distance = "15.53",
                        duration = "03:20",
                        maxElevation = "2132",
                        photos = persistentListOf(ArchivePhoto(1L, "창의문", "", Instant.EPOCH)),
                    ),
                ),
            ),
            expanded = false,
            onExpandedChange = {},
            onBackClick = {},
        )
    }
}

@PreviewDeviceSizes
@Composable
private fun ArchiveDetailScreenLoadingPreview() {
    val loadingState = remember { RouteeLoadingState() }
    CompositionLocalProvider(LocalRouteeLoadingState provides loadingState) {
        RouteePreviewTheme {
            Box(Modifier.fillMaxSize()) {
                ArchiveDetailScreen(
                    uiState = ArchiveDetailUiState(),
                    expanded = false,
                    onExpandedChange = {},
                    onBackClick = {},
                )
                RouteeLoadingHost(state = loadingState, showDelayMillis = 0)
            }
        }
    }
}
