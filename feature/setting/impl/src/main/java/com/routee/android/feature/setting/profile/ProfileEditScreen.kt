package com.routee.android.feature.setting.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.result.ResultEffect
import com.routee.android.core.designsystem.component.RouteeBgEllipse
import com.routee.android.core.designsystem.component.RouteeCtaPrimaryButton
import com.routee.android.core.designsystem.component.RouteePageNavBar
import com.routee.android.core.designsystem.component.dialog.RouteeDialogAction
import com.routee.android.core.designsystem.component.dialog.RouteeVerticalActionDialog
import com.routee.android.core.designsystem.component.message.LocalRouteeMessageState
import com.routee.android.core.designsystem.component.message.RouteeMessageDuration
import com.routee.android.core.designsystem.component.textfield.RouteeNicknameTextField
import com.routee.android.core.designsystem.extension.clearFocus
import com.routee.android.core.designsystem.preview.PreviewDeviceSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.core.ui.CollectSideEffect
import com.routee.android.core.ui.error.RouteeErrorScreen
import com.routee.android.core.ui.error.RouteeErrorType
import com.routee.android.core.ui.loading.RouteeLoadingOverlay
import com.routee.android.feature.setting.R
import com.routee.android.feature.setting.component.profile.ProfileImage
import com.routee.android.feature.setting.profile.crop.ProfileImageCropResult
import com.routee.android.feature.setting.profile.model.ProfileEditSideEffect
import com.routee.android.feature.setting.profile.model.ProfileEditUiState

private val ProfileImageTopSpacing = 51.dp
private val NicknameTopSpacing = 42.dp
private val BottomSpacing = 28.dp

@Composable
internal fun ProfileEditRoute(
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onNavigateToImageCrop: (imageUri: String) -> Unit,
    viewModel: ProfileEditViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val messageState = LocalRouteeMessageState.current
    val savedText = stringResource(R.string.setting_profile_saved_toast)
    val networkErrorText = stringResource(R.string.setting_network_error_toast)
    val unknownErrorText = stringResource(R.string.setting_unknown_error_toast)
    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) onNavigateToImageCrop(uri.toString())
    }

    viewModel.sideEffect.CollectSideEffect { sideEffect ->
        when (sideEffect) {
            ProfileEditSideEffect.OpenGallery -> photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
            )

            ProfileEditSideEffect.ShowSavedToast -> messageState.showToast(text = savedText)

            ProfileEditSideEffect.ShowNetworkErrorToast -> messageState.showToast(
                text = networkErrorText,
                duration = RouteeMessageDuration.LONG,
            )

            ProfileEditSideEffect.ShowUnknownErrorToast -> messageState.showToast(text = unknownErrorText)
        }
    }

    ResultEffect<ProfileImageCropResult> { result ->
        when (result) {
            is ProfileImageCropResult.Complete -> viewModel.onProfileImageCropComplete(result.imageUri)
            ProfileImageCropResult.Cancel -> viewModel.onProfileImageCropCancel()
        }
    }

    if (uiState.isLoading) RouteeLoadingOverlay()

    ProfileEditScreen(
        uiState = uiState,
        nicknameState = viewModel.nicknameState,
        onBackClick = onBackClick,
        onHomeClick = onHomeClick,
        onRetryClick = viewModel::onRetryClick,
        onProfileImageClick = viewModel::onProfileImageClick,
        onNicknameFocusChange = viewModel::onNicknameFocusChange,
        onSaveClick = viewModel::onSaveClick,
    )

    if (uiState.isPhotoDialogVisible) {
        RouteeVerticalActionDialog(
            title = stringResource(R.string.setting_profile_photo_dialog_title),
            actions = listOf(
                RouteeDialogAction(
                    text = stringResource(R.string.setting_profile_photo_dialog_album),
                    onClick = viewModel::onAlbumClick,
                ),
                RouteeDialogAction(
                    text = stringResource(R.string.setting_profile_photo_dialog_default),
                    onClick = viewModel::onDefaultImageClick,
                ),
            ),
            onDismissRequest = viewModel::onPhotoDialogDismiss,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileEditScreen(
    uiState: ProfileEditUiState,
    nicknameState: TextFieldState,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onRetryClick: () -> Unit,
    onProfileImageClick: () -> Unit,
    onNicknameFocusChange: (Boolean) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (uiState.isLoadFailed) {
        RouteeErrorScreen(
            type = RouteeErrorType.Unknown,
            onHomeClick = onHomeClick,
            onRetryClick = onRetryClick,
            modifier = modifier.fillMaxSize().navigationBarsPadding(),
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
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            RouteePageNavBar(
                onBackClick = onBackClick,
                title = stringResource(R.string.setting_profile_title),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = RouteeTheme.spacing.Spacing32,
                        top = ProfileImageTopSpacing,
                        end = RouteeTheme.spacing.Spacing32,
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ProfileImage(
                    imageUrl = uiState.profileImageUrl,
                    onClick = onProfileImageClick,
                )
                Spacer(modifier = Modifier.height(NicknameTopSpacing))
                RouteeNicknameTextField(
                    state = nicknameState,
                    placeholder = stringResource(R.string.setting_profile_nickname_placeholder),
                    guideText = stringResource(R.string.setting_profile_nickname_guide),
                    validation = uiState.validation,
                    onKeyboardAction = { focusManager.clearFocus() },
                    showEditIcon = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { onNicknameFocusChange(it.hasFocus) },
                )
            }
            RouteeCtaPrimaryButton(
                text = stringResource(R.string.setting_profile_save),
                onClick = onSaveClick,
                enabled = uiState.isSaveEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = RouteeTheme.spacing.Spacing16,
                        end = RouteeTheme.spacing.Spacing16,
                        bottom = BottomSpacing,
                    ),
            )
        }
    }
}

@PreviewDeviceSizes
@Composable
private fun ProfileEditScreenPreview() {
    RouteePreviewTheme {
        ProfileEditScreen(
            uiState = ProfileEditUiState(isLoading = false, savedNickname = "관악산 날다람쥐", nickname = "관악산 날다람쥐"),
            nicknameState = rememberTextFieldState("관악산 날다람쥐"),
            onBackClick = {},
            onHomeClick = {},
            onRetryClick = {},
            onProfileImageClick = {},
            onNicknameFocusChange = {},
            onSaveClick = {},
        )
    }
}
