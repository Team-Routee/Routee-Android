package com.routee.android.feature.recordedit.editor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.routee.android.core.designsystem.component.RouteeBgEllipse
import com.routee.android.core.designsystem.component.RouteePageNavBar
import com.routee.android.core.designsystem.component.dialog.RouteeTwoButtonDialog
import com.routee.android.core.designsystem.extension.noRippleClickable
import com.routee.android.core.designsystem.preview.PreviewDeviceSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.core.ui.CollectSideEffect
import com.routee.android.core.ui.UiState
import com.routee.android.core.ui.error.RouteeErrorScreen
import com.routee.android.core.ui.error.RouteeErrorType
import com.routee.android.core.ui.loading.RouteeLoadingOverlay
import com.routee.android.feature.recordedit.R
import com.routee.android.feature.recordedit.component.editor.BrightnessSlider
import com.routee.android.feature.recordedit.component.editor.ColorPalette
import com.routee.android.feature.recordedit.component.editor.RecapEditNavBar
import com.routee.android.feature.recordedit.component.editor.RecapPreview
import com.routee.android.feature.recordedit.editor.model.RecapEditorContent
import com.routee.android.feature.recordedit.editor.model.RecapEditorSideEffect
import com.routee.android.feature.recordedit.editor.model.RecapEditorUiState
import com.routee.android.feature.recordedit.editor.model.RecapRecordUiModel
import com.routee.android.feature.recordedit.model.RecapColor
import com.routee.android.feature.recordedit.model.RecapEditTab

// recap_image 9:16
private const val PREVIEW_ASPECT_RATIO = 9f / 16f

@Composable
internal fun RecapEditorRoute(
    viewModel: RecapEditorViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    viewModel.sideEffect.CollectSideEffect { sideEffect ->
        when (sideEffect) {
            RecapEditorSideEffect.NavigateBack -> onNavigateBack()
        }
    }

    BackHandler(onBack = viewModel::onBackClick)

    RecapEditorScreen(
        uiState = uiState,
        onBackClick = viewModel::onBackClick,
        onCompleteClick = viewModel::onCompleteClick,
        onTabClick = viewModel::onTabClick,
        onPreviewClick = viewModel::onPreviewClick,
        onBrightnessChange = viewModel::onBrightnessChange,
        onColorClick = viewModel::onColorClick,
        onEditStopConfirm = viewModel::onEditStopConfirm,
        onEditStopDismiss = viewModel::onEditStopDismiss,
        onRetryClick = viewModel::onRetryClick,
        onHomeClick = onNavigateToHome,
    )
}

@Composable
private fun RecapEditorScreen(
    uiState: RecapEditorUiState,
    onBackClick: () -> Unit,
    onCompleteClick: () -> Unit,
    onTabClick: (RecapEditTab) -> Unit,
    onPreviewClick: () -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onColorClick: (RecapColor) -> Unit,
    onEditStopConfirm: () -> Unit,
    onEditStopDismiss: () -> Unit,
    onRetryClick: () -> Unit,
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (val content = uiState.content) {
        UiState.Loading, UiState.Empty -> RouteeLoadingOverlay()

        is UiState.Failure -> RouteeErrorScreen(
            type = RouteeErrorType.Unknown,
            onHomeClick = onHomeClick,
            onRetryClick = onRetryClick,
            modifier = modifier,
        )

        is UiState.Success -> RecapEditorContent(
            uiState = uiState,
            content = content.data,
            onBackClick = onBackClick,
            onCompleteClick = onCompleteClick,
            onTabClick = onTabClick,
            onPreviewClick = onPreviewClick,
            onBrightnessChange = onBrightnessChange,
            onColorClick = onColorClick,
            modifier = modifier,
        )
    }

    if (uiState.isEditStopDialogVisible) {
        RouteeTwoButtonDialog(
            title = stringResource(R.string.record_edit_editor_stop_dialog_title),
            description = stringResource(R.string.record_edit_editor_stop_dialog_description),
            leftButtonText = stringResource(R.string.record_edit_editor_dialog_cancel),
            rightButtonText = stringResource(R.string.record_edit_editor_dialog_confirm),
            onRightButtonClick = onEditStopConfirm,
            onDismissRequest = onEditStopDismiss,
        )
    }
}

@Composable
private fun RecapEditorContent(
    uiState: RecapEditorUiState,
    content: RecapEditorContent,
    onBackClick: () -> Unit,
    onCompleteClick: () -> Unit,
    onTabClick: (RecapEditTab) -> Unit,
    onPreviewClick: () -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onColorClick: (RecapColor) -> Unit,
    modifier: Modifier = Modifier,
) {
    RouteeBgEllipse(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            RouteePageNavBar(
                onBackClick = onBackClick,
                actions = {
                    Text(
                        text = stringResource(R.string.record_edit_editor_complete),
                        style = RouteeTheme.typography.labelSb18,
                        color = RouteeTheme.colors.white,
                        modifier = Modifier
                            .noRippleClickable(role = Role.Button, onClick = onCompleteClick)
                            .padding(
                                horizontal = RouteeTheme.spacing.Spacing16,
                                vertical = RouteeTheme.spacing.Spacing10,
                            ),
                    )
                },
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = RouteeTheme.spacing.Spacing12),
            ) {
                RecapPreview(
                    backgroundUrl = content.mapImageUrl,
                    overlayOpacity = uiState.overlayOpacity,
                    recapColor = uiState.recapColor,
                    record = content.record,
                    track = content.track,
                    onBackgroundClick = onPreviewClick,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = RouteeTheme.spacing.Spacing16)
                        .aspectRatio(PREVIEW_ASPECT_RATIO),
                )
                when (uiState.selectedTab) {
                    RecapEditTab.BRIGHTNESS -> BrightnessSlider(
                        value = uiState.brightness,
                        onValueChange = onBrightnessChange,
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )

                    RecapEditTab.COLOR -> ColorPalette(
                        selectedColor = uiState.recapColor,
                        onColorClick = onColorClick,
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )

                    RecapEditTab.BACKGROUND, RecapEditTab.STICKER, null -> Unit
                }
            }
            RecapEditNavBar(
                selectedTab = uiState.selectedTab,
                onTabClick = onTabClick,
            )
        }
    }
}

private class RecapEditorUiStatePreviewParameterProvider : PreviewParameterProvider<RecapEditorUiState> {
    private val content = UiState.Success(
        RecapEditorContent.EMPTY.copy(
            record = RecapRecordUiModel.from(
                distanceMeters = 15_500,
                durationSeconds = 12_000,
                maxElevationMeters = 2132,
            ),
        ),
    )

    override val values = sequenceOf(
        RecapEditorUiState(content = content),
        RecapEditorUiState(content = content, selectedTab = RecapEditTab.BRIGHTNESS),
        RecapEditorUiState(content = content, selectedTab = RecapEditTab.COLOR, recapColor = RecapColor.ORANGE),
    )
}

@PreviewDeviceSizes
@Composable
private fun RecapEditorScreenPreview(
    @PreviewParameter(RecapEditorUiStatePreviewParameterProvider::class) uiState: RecapEditorUiState,
) {
    RouteePreviewTheme {
        RecapEditorScreen(
            uiState = uiState,
            onBackClick = {},
            onCompleteClick = {},
            onTabClick = {},
            onPreviewClick = {},
            onBrightnessChange = {},
            onColorClick = {},
            onEditStopConfirm = {},
            onEditStopDismiss = {},
            onRetryClick = {},
            onHomeClick = {},
        )
    }
}
