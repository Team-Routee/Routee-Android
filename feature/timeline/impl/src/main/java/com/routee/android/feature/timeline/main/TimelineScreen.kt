package com.routee.android.feature.timeline.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.routee.android.core.common.util.suspendRunCatching
import com.routee.android.core.designsystem.R as DesignSystemR
import com.routee.android.core.designsystem.component.RouteeBgEllipse
import com.routee.android.core.designsystem.component.RouteeCtaPrimaryButton
import com.routee.android.core.designsystem.component.RouteePageNavBar
import com.routee.android.core.designsystem.component.RouteeWorkoutMetricCard
import com.routee.android.core.designsystem.component.dialog.RouteeTwoButtonDialog
import com.routee.android.core.designsystem.component.message.LocalRouteeMessageState
import com.routee.android.core.designsystem.component.message.routeeMessageAnchor
import com.routee.android.core.designsystem.component.textfield.RouteeBasicTextField
import com.routee.android.core.designsystem.component.textfield.RouteeRecordSummaryTextField
import com.routee.android.core.designsystem.extension.clearFocus
import com.routee.android.core.designsystem.extension.noRippleClickable
import com.routee.android.core.designsystem.preview.PreviewDeviceSizes
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.core.ui.CollectSideEffect
import com.routee.android.core.ui.UiState
import com.routee.android.core.ui.loading.RouteeLoadingOverlay
import com.routee.android.core.ui.photo.rememberGalleryPhotoSaveState
import com.routee.android.feature.timeline.R
import com.routee.android.feature.timeline.component.MyRouteEditor
import com.routee.android.feature.timeline.component.RoutePoint
import com.routee.android.feature.timeline.component.TimelinePhotoSection
import com.routee.android.feature.timeline.main.model.TimelineDraft
import com.routee.android.feature.timeline.main.model.TimelineRequestResult
import com.routee.android.feature.timeline.main.model.TimelineSaveDestination
import com.routee.android.feature.timeline.main.model.TimelineSideEffect
import com.routee.android.feature.timeline.main.model.TimelineUiState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

private val RecordDateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
private val SectionTopSpacing = 52.dp
private val SectionSpacing = 48.dp
private val CtaTopSpacing = 40.dp
private val BottomSpacing = 28.dp

@Composable
internal fun TimelineRoute(
    viewModel: TimelineViewModel,
    onExit: () -> Unit,
    onSaveSuccess: (TimelineSaveDestination) -> Unit,
    onSaveRecord: suspend (TimelineDraft) -> TimelineRequestResult,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showExitDialog by rememberSaveable(viewModel.activityId) { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val messageState = LocalRouteeMessageState.current
    val networkErrorText = stringResource(R.string.timeline_network_error_toast)
    val unknownErrorText = stringResource(R.string.timeline_unknown_error_toast)
    val deleteNetworkErrorText = stringResource(R.string.timeline_delete_network_error_toast)
    val deleteUnknownErrorText = stringResource(R.string.timeline_delete_unknown_error_toast)

    BackHandler {
        if (!isSaving) showExitDialog = true
    }

    // TODO P1 기획: 경로 조회 실패 문구 (저장 실패 토스트 문구 재사용)
    viewModel.sideEffect.CollectSideEffect { sideEffect ->
        when (sideEffect) {
            is TimelineSideEffect.TrackLoadFailed -> {
                val isNetwork = sideEffect.result == TimelineRequestResult.NetworkFailed
                messageState.showToast(text = if (isNetwork) networkErrorText else unknownErrorText)
            }
        }
    }

    val gallerySave = rememberGalleryPhotoSaveState(viewModel::savePhoto)
    val recordDate = remember(viewModel.endedAtEpochMillis) {
        Instant.ofEpochMilli(viewModel.endedAtEpochMillis)
            .atZone(ZoneId.systemDefault())
            .format(RecordDateFormatter)
    }
    val defaultTitle = stringResource(R.string.timeline_default_record_title, recordDate)
    val focusManager = LocalFocusManager.current
    val photos = uiState.photos
    val hasPhotoSection = photos is UiState.Loading || (photos as? UiState.Success)?.data?.isNotEmpty() == true
    fun finishTitleEdit() {
        viewModel.onTitleEditFinish()
        focusManager.clearFocus()
    }
    fun deleteAndExit() {
        if (isSaving) return
        isSaving = true
        scope.launch {
            val result = suspendRunCatching { viewModel.deleteRecord() }.getOrDefault(TimelineRequestResult.Failed)
            isSaving = false
            // TODO P1 기획: 삭제 성공 토스트 여부 (아카이브는 "기록이 삭제되었습니다.")
            // 실패해도 나감 (기록은 남고 아카이브에서 삭제 가능)
            when (result) {
                TimelineRequestResult.Success -> Unit
                TimelineRequestResult.NetworkFailed -> messageState.showToast(text = deleteNetworkErrorText)
                TimelineRequestResult.Failed -> messageState.showToast(text = deleteUnknownErrorText)
            }
            onExit()
        }
    }
    fun saveAndNavigate(destination: TimelineSaveDestination) {
        if (isSaving || showExitDialog) return
        isSaving = true
        scope.launch {
            val result = suspendRunCatching { onSaveRecord(viewModel.draft(defaultTitle)) }
                .getOrDefault(TimelineRequestResult.Failed)
            isSaving = false
            when (result) {
                TimelineRequestResult.Success -> onSaveSuccess(destination)
                TimelineRequestResult.NetworkFailed -> messageState.showToast(text = networkErrorText)
                TimelineRequestResult.Failed -> messageState.showToast(text = unknownErrorText)
            }
        }
    }
    TimelineScreen(
        uiState = uiState,
        titleState = viewModel.titleState,
        defaultTitle = defaultTitle,
        isTitleEditing = uiState.isTitleEditing,
        onBackClick = { if (!isSaving) showExitDialog = true },
        onCompleteClick = {
            finishTitleEdit()
            saveAndNavigate(TimelineSaveDestination.Home)
        },
        onEditTitleClick = { viewModel.onEditTitleClick(defaultTitle) },
        onTitleEditFinish = ::finishTitleEdit,
        onOutsideClick = { finishTitleEdit() },
        onRecordEditClick = {
            finishTitleEdit()
            saveAndNavigate(TimelineSaveDestination.RecordEdit)
        },
        modifier = modifier,
        routeMap = { TimelineRouteMap(track = uiState.track) },
        photoSection = if (hasPhotoSection) {
            {
                TimelinePhotoSection(
                    photos = photos,
                    recordDate = recordDate,
                    onSaveClick = { index, photo ->
                        gallerySave.onSavePhoto(index, photo.imageUrl, photo.createdAt.toEpochMilli())
                    },
                    savedPhotoIndex = gallerySave.savedPhotoIndex,
                    saveMessage = gallerySave.saveMessage,
                )
            }
        } else {
            null
        },
        myRoute = {
            MyRouteEditor(
                points = viewModel.points,
                onMovePoint = viewModel::onMovePoint,
                onRemovePoint = { index ->
                    viewModel.onRemovePoint(index)
                    focusManager.clearFocus()
                },
                onAddPoint = {
                    focusManager.clearFocus()
                    viewModel.onAddPoint()
                },
                isLoading = photos is UiState.Loading,
            )
        },
    )

    if (isSaving) RouteeLoadingOverlay()

    if (showExitDialog) {
        RouteeTwoButtonDialog(
            title = stringResource(R.string.timeline_exit_title),
            description = stringResource(R.string.timeline_exit_description),
            leftButtonText = stringResource(R.string.timeline_cancel),
            rightButtonText = stringResource(R.string.timeline_exit),
            onRightButtonClick = {
                showExitDialog = false
                deleteAndExit()
            },
            onDismissRequest = { showExitDialog = false },
        )
    }
}

@Composable
private fun TimelineScreen(
    uiState: TimelineUiState,
    titleState: TextFieldState,
    defaultTitle: String,
    isTitleEditing: Boolean,
    onBackClick: () -> Unit,
    onCompleteClick: () -> Unit,
    onEditTitleClick: () -> Unit,
    onTitleEditFinish: () -> Unit,
    onOutsideClick: () -> Unit,
    onRecordEditClick: () -> Unit,
    modifier: Modifier = Modifier,
    routeMap: (@Composable () -> Unit)? = null,
    photoSection: (@Composable () -> Unit)? = null,
    myRoute: (@Composable () -> Unit)? = null,
) {
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current
    var buttonAreaHeight by remember { mutableStateOf(0.dp) }
    val coordinates = remember { TitleEditCoordinates() }
    val currentOnOutsideClick by rememberUpdatedState(onOutsideClick)
    RouteeBgEllipse(
        modifier = modifier
            .onGloballyPositioned { coordinates.screen = it }
            .pointerInput(isTitleEditing) {
                if (!isTitleEditing) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    val screen = coordinates.screen ?: return@awaitEachGesture
                    val title = coordinates.title ?: return@awaitEachGesture
                    val position = title.localPositionOf(screen, down.position)
                    if (position.x < 0 || position.x > title.size.width ||
                        position.y < 0 || position.y > title.size.height
                    ) {
                        currentOnOutsideClick()
                        focusManager.clearFocus()
                    }
                }
            },
    ) {
        Box(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Column(modifier = Modifier.fillMaxSize()) {
                RouteePageNavBar(
                    onBackClick = onBackClick,
                    modifier = Modifier.fillMaxWidth(),
                    actions = {
                        Text(
                            text = stringResource(R.string.timeline_complete),
                            style = RouteeTheme.typography.labelSb18,
                            color = RouteeTheme.colors.white,
                            modifier = Modifier
                                .noRippleClickable(onClick = onCompleteClick)
                                .semantics { role = Role.Button }
                                .padding(
                                    horizontal = RouteeTheme.spacing.Spacing16,
                                    vertical = RouteeTheme.spacing.Spacing10,
                                ),
                        )
                    },
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                        .clearFocus(focusManager, onOutsideClick),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = RouteeTheme.spacing.Spacing16),
                    ) {
                        RouteeRecordSummaryTextField(
                            state = titleState,
                            defaultTitle = defaultTitle,
                            isEditing = isTitleEditing,
                            onEditClick = onEditTitleClick,
                            onEditComplete = onTitleEditFinish,
                            modifier = Modifier.onGloballyPositioned { coordinates.title = it },
                        )
                        Spacer(Modifier.height(RouteeTheme.spacing.Spacing8))
                        RouteeWorkoutMetricCard(
                            distance = uiState.distance,
                            duration = uiState.duration,
                            maxElevation = uiState.maxElevation,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        if (routeMap != null) {
                            Spacer(Modifier.height(RouteeTheme.spacing.Spacing8))
                            routeMap()
                        }
                    }
                    if (photoSection != null || myRoute != null) {
                        Spacer(Modifier.height(SectionTopSpacing))
                    }
                    photoSection?.invoke()
                    if (photoSection != null && myRoute != null) {
                        Spacer(Modifier.height(SectionSpacing))
                    }
                    if (myRoute != null) {
                        Column(modifier = Modifier.padding(horizontal = RouteeTheme.spacing.Spacing16)) {
                            myRoute()
                        }
                    }
                    Spacer(Modifier.height(CtaTopSpacing + buttonAreaHeight))
                }
            }
            RouteeCtaPrimaryButton(
                text = stringResource(R.string.timeline_edit_record),
                onClick = onRecordEditClick,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .onSizeChanged { buttonAreaHeight = with(density) { it.height.toDp() } }
                    .routeeMessageAnchor()
                    .padding(
                        start = RouteeTheme.spacing.Spacing16,
                        end = RouteeTheme.spacing.Spacing16,
                        bottom = BottomSpacing,
                    ),
            )
        }
    }
}

private class TitleEditCoordinates {
    var screen: LayoutCoordinates? = null
    var title: LayoutCoordinates? = null
}

@PreviewDeviceSizes
@Composable
private fun TimelineScreenPreview() {
    RouteeTheme {
        var isTitleEditing by remember { mutableStateOf(false) }
        val points = remember {
            listOf("창의문", "청운대", "말바위")
                .mapIndexed { index, name -> RoutePoint(index.toLong(), TextFieldState(name)) }
                .toMutableStateList()
        }
        TimelineScreen(
            uiState = TimelineUiState(
                distance = "15.53",
                duration = "03:20",
                maxElevation = "2132",
            ),
            titleState = rememberTextFieldState("2026.07.18 기록"),
            defaultTitle = "2026.07.18 기록",
            isTitleEditing = isTitleEditing,
            onBackClick = {},
            onCompleteClick = {},
            onEditTitleClick = { isTitleEditing = true },
            onTitleEditFinish = { isTitleEditing = false },
            onOutsideClick = {},
            onRecordEditClick = {},
            myRoute = {
                MyRouteEditor(
                    points = points,
                    onMovePoint = { from, to -> points.add(to, points.removeAt(from)) },
                    onRemovePoint = points::removeAt,
                    onAddPoint = {
                        points.add(RoutePoint((points.maxOfOrNull { it.id } ?: -1L) + 1L, TextFieldState()))
                    },
                )
            },
        )
    }
}
