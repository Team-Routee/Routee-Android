package com.routee.android.feature.setting.profile

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
import com.routee.android.core.designsystem.component.RouteeBgEllipse
import com.routee.android.core.designsystem.component.RouteeCtaPrimaryButton
import com.routee.android.core.designsystem.component.RouteePageNavBar
import com.routee.android.core.designsystem.component.message.LocalRouteeMessageState
import com.routee.android.core.designsystem.component.message.routeeMessageAnchor
import com.routee.android.core.designsystem.component.textfield.RouteeNicknameTextField
import com.routee.android.core.designsystem.extension.clearFocus
import com.routee.android.core.designsystem.preview.PreviewDeviceSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.core.ui.CollectSideEffect
import com.routee.android.feature.setting.R
import com.routee.android.feature.setting.component.profile.ProfileImage
import com.routee.android.feature.setting.profile.model.ProfileEditSideEffect
import com.routee.android.feature.setting.profile.model.ProfileEditUiState

private val ProfileImageTopSpacing = 51.dp
private val NicknameTopSpacing = 42.dp
private val BottomSpacing = 28.dp

@Composable
internal fun ProfileEditRoute(
    onBackClick: () -> Unit,
    viewModel: ProfileEditViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val messageState = LocalRouteeMessageState.current
    val savedText = stringResource(R.string.setting_profile_saved_toast)

    viewModel.sideEffect.CollectSideEffect { sideEffect ->
        when (sideEffect) {
            ProfileEditSideEffect.ShowSavedToast -> messageState.showToast(text = savedText)
        }
    }

    ProfileEditScreen(
        uiState = uiState,
        nicknameState = viewModel.nicknameState,
        onBackClick = onBackClick,
        // TODO 사진 설정 모달, 갤러리
        onProfileImageClick = {},
        onNicknameFocusChange = viewModel::onNicknameFocusChange,
        onSaveClick = viewModel::onSaveClick,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileEditScreen(
    uiState: ProfileEditUiState,
    nicknameState: TextFieldState,
    onBackClick: () -> Unit,
    onProfileImageClick: () -> Unit,
    onNicknameFocusChange: (Boolean) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                    )
                    .routeeMessageAnchor(),
            )
        }
    }
}

@PreviewDeviceSizes
@Composable
private fun ProfileEditScreenPreview() {
    RouteePreviewTheme {
        ProfileEditScreen(
            uiState = ProfileEditUiState(savedNickname = "관악산 날다람쥐", nickname = "관악산 날다람쥐"),
            nicknameState = rememberTextFieldState("관악산 날다람쥐"),
            onBackClick = {},
            onProfileImageClick = {},
            onNicknameFocusChange = {},
            onSaveClick = {},
        )
    }
}
