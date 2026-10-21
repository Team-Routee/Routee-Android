package com.routee.android.feature.timeline.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.routee.android.core.designsystem.R as DesignSystemR
import com.routee.android.core.designsystem.component.RouteeBgEllipse
import com.routee.android.core.designsystem.component.RouteeCtaPrimaryButton
import com.routee.android.core.designsystem.component.RouteePageNavBar
import com.routee.android.core.designsystem.component.RouteeWorkoutMetricCard
import com.routee.android.core.designsystem.component.textfield.RouteeBasicTextField
import com.routee.android.core.designsystem.extension.clearFocus
import com.routee.android.core.designsystem.extension.noRippleClickable
import com.routee.android.core.designsystem.preview.PreviewDeviceSizes
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.feature.timeline.R
import com.routee.android.feature.timeline.api.TimelinePhoto
import com.routee.android.feature.timeline.component.MyRouteEditor
import com.routee.android.feature.timeline.component.RoutePoint
import com.routee.android.feature.timeline.component.TimelinePhotoSection
import com.routee.android.feature.timeline.main.model.TimelineDraft
import com.routee.android.feature.timeline.main.model.TimelineUiState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val RecordDateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
private val EditIconTouchSize = 44.dp
private val SectionTopSpacing = 52.dp
private val SectionSpacing = 48.dp
private val CtaTopSpacing = 40.dp
private val BottomSpacing = 28.dp
private const val MAX_TITLE_LENGTH = 16

@Composable
internal fun TimelineRoute(
    viewModel: TimelineViewModel,
    onBackClick: () -> Unit,
    onCompleteClick: (TimelineDraft) -> Unit,
    onRecordEditClick: (TimelineDraft) -> Unit,
    onPhotoSaveClick: (TimelinePhoto) -> Unit,
    modifier: Modifier = Modifier,
    routeMap: (@Composable () -> Unit)? = null,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val recordDate = remember(viewModel.endedAtEpochMillis) {
        Instant.ofEpochMilli(viewModel.endedAtEpochMillis)
            .atZone(ZoneId.systemDefault())
            .format(RecordDateFormatter)
    }
    val defaultTitle = stringResource(R.string.timeline_default_record_title, recordDate)
    val focusManager = LocalFocusManager.current
    fun finishTitleEdit() {
        viewModel.onTitleEditFinish()
        focusManager.clearFocus()
    }
    TimelineScreen(
        uiState = uiState,
        photos = viewModel.photos,
        titleState = viewModel.titleState,
        defaultTitle = defaultTitle,
        isTitleEditing = uiState.isTitleEditing,
        onBackClick = onBackClick,
        onCompleteClick = {
            finishTitleEdit()
            onCompleteClick(viewModel.draft(defaultTitle))
        },
        onEditTitleClick = { viewModel.onEditTitleClick(defaultTitle) },
        onTitleEditFinish = ::finishTitleEdit,
        onTitleFocusLoss = viewModel::onTitleEditFinish,
        onOutsideClick = { finishTitleEdit() },
        onRecordEditClick = {
            finishTitleEdit()
            onRecordEditClick(viewModel.draft(defaultTitle))
        },
        onPhotoSaveClick = onPhotoSaveClick,
        modifier = modifier,
        routeMap = routeMap,
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
            )
        },
    )
}

@Composable
private fun TimelineScreen(
    uiState: TimelineUiState,
    photos: List<TimelinePhoto>,
    titleState: TextFieldState,
    defaultTitle: String,
    isTitleEditing: Boolean,
    onBackClick: () -> Unit,
    onCompleteClick: () -> Unit,
    onEditTitleClick: () -> Unit,
    onTitleEditFinish: () -> Unit,
    onTitleFocusLoss: () -> Unit,
    onOutsideClick: () -> Unit,
    onRecordEditClick: () -> Unit,
    onPhotoSaveClick: (TimelinePhoto) -> Unit,
    modifier: Modifier = Modifier,
    routeMap: (@Composable () -> Unit)? = null,
    myRoute: (@Composable () -> Unit)? = null,
) {
    val focusManager = LocalFocusManager.current
    RouteeBgEllipse(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
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
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .clearFocus(focusManager, onOutsideClick),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = RouteeTheme.spacing.Spacing16),
                ) {
                    TimelineTitleRow(
                        titleState = titleState,
                        defaultTitle = defaultTitle,
                        isEditing = isTitleEditing,
                        onEditClick = onEditTitleClick,
                        onEditFinish = onTitleEditFinish,
                        onFocusLoss = onTitleFocusLoss,
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
                if (photos.isNotEmpty() || myRoute != null) {
                    Spacer(Modifier.height(SectionTopSpacing))
                }
                if (photos.isNotEmpty()) {
                    TimelinePhotoSection(photos = photos, onSaveClick = onPhotoSaveClick)
                }
                if (photos.isNotEmpty() && myRoute != null) {
                    Spacer(Modifier.height(SectionSpacing))
                }
                if (myRoute != null) {
                    Column(modifier = Modifier.padding(horizontal = RouteeTheme.spacing.Spacing16)) {
                        myRoute()
                    }
                }
                Spacer(Modifier.height(CtaTopSpacing))
                RouteeCtaPrimaryButton(
                    text = stringResource(R.string.timeline_edit_record),
                    onClick = onRecordEditClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = RouteeTheme.spacing.Spacing16),
                )
                Spacer(Modifier.height(BottomSpacing))
            }
        }
    }
}

@Composable
private fun TimelineTitleRow(
    titleState: TextFieldState,
    defaultTitle: String,
    isEditing: Boolean,
    onEditClick: () -> Unit,
    onEditFinish: () -> Unit,
    onFocusLoss: () -> Unit,
) {
    var hadFocus by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isEditing) {
            val focusRequester = remember { FocusRequester() }
            LaunchedEffect(focusRequester) { focusRequester.requestFocus() }
            RouteeBasicTextField(
                state = titleState,
                textStyle = RouteeTheme.typography.titleSb20,
                maxLength = MAX_TITLE_LENGTH,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                onKeyboardAction = { onEditFinish() },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            hadFocus = true
                        } else if (hadFocus) {
                            hadFocus = false
                            onFocusLoss()
                        }
                    },
            )
        } else {
            Text(
                text = titleState.text.toString().ifEmpty { defaultTitle },
                style = RouteeTheme.typography.titleSb20,
                color = RouteeTheme.colors.white,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }
        Image(
            imageVector = ImageVector.vectorResource(DesignSystemR.drawable.ic_edit_sm_line_white),
            contentDescription = stringResource(R.string.timeline_edit_title),
            modifier = Modifier
                .size(EditIconTouchSize)
                .noRippleClickable(onClick = onEditClick)
                .semantics { role = Role.Button }
                .padding(RouteeTheme.spacing.Spacing10),
        )
    }
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
            photos = emptyList(),
            titleState = rememberTextFieldState("2026.07.18 기록"),
            defaultTitle = "2026.07.18 기록",
            isTitleEditing = isTitleEditing,
            onBackClick = {},
            onCompleteClick = {},
            onEditTitleClick = { isTitleEditing = true },
            onTitleEditFinish = { isTitleEditing = false },
            onTitleFocusLoss = { isTitleEditing = false },
            onOutsideClick = {},
            onRecordEditClick = {},
            onPhotoSaveClick = {},
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
