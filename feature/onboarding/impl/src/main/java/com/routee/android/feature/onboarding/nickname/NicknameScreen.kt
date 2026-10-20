package com.routee.android.feature.onboarding.nickname

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.routee.android.core.designsystem.component.RouteeBgEllipse
import com.routee.android.core.designsystem.component.RouteeCtaPrimaryButton
import com.routee.android.core.designsystem.component.RouteePageNavBar
import com.routee.android.core.designsystem.component.message.LocalRouteeMessageState
import com.routee.android.core.designsystem.component.message.RouteeMessageDuration
import com.routee.android.core.designsystem.component.textfield.RouteeNicknameTextField
import com.routee.android.core.designsystem.extension.clearFocus
import com.routee.android.core.designsystem.extension.imePaddingAbove
import com.routee.android.core.designsystem.extension.scrollToEndOnFocus
import com.routee.android.core.designsystem.preview.PreviewDeviceSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.core.ui.CollectSideEffect
import com.routee.android.feature.onboarding.R
import com.routee.android.feature.onboarding.nickname.model.NicknameSideEffect
import com.routee.android.feature.onboarding.nickname.model.NicknameUiState

private val ContentTopPadding = 113.dp
private val TitleToFieldSpacing = 89.dp
private val ButtonBottomPadding = 75.dp

@Composable
internal fun NicknameRoute(
    viewModel: NicknameViewModel,
    onBackClick: () -> Unit,
    onNavigateToHome: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val messageState = LocalRouteeMessageState.current
    val networkErrorMessage = stringResource(R.string.onboarding_network_error_toast)
    val unknownErrorMessage = stringResource(R.string.onboarding_unknown_error_toast)

    viewModel.sideEffect.CollectSideEffect { sideEffect ->
        when (sideEffect) {
            NicknameSideEffect.NavigateToHome -> onNavigateToHome()

            NicknameSideEffect.ShowNetworkErrorToast -> messageState.showToast(
                text = networkErrorMessage,
                duration = RouteeMessageDuration.SHORT,
            )

            NicknameSideEffect.ShowUnknownErrorToast -> messageState.showToast(
                text = unknownErrorMessage,
                duration = RouteeMessageDuration.LONG,
            )
        }
    }

    NicknameScreen(
        uiState = uiState,
        nicknameState = viewModel.nicknameState,
        onBackClick = onBackClick,
        onNicknameFocusChange = viewModel::onFocusChange,
        onStartClick = viewModel::onStartClick,
    )
}

@Composable
private fun NicknameScreen(
    uiState: NicknameUiState,
    nicknameState: TextFieldState,
    onBackClick: () -> Unit,
    onNicknameFocusChange: (Boolean) -> Unit,
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current
    val scrollState = rememberScrollState()
    var buttonAreaHeight by remember { mutableStateOf(0.dp) }

    RouteeBgEllipse(modifier = modifier.clearFocus(focusManager)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            RouteePageNavBar(onBackClick = onBackClick)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .imePaddingAbove(buttonAreaHeight)
                    .scrollToEndOnFocus(scrollState)
                    .verticalScroll(scrollState)
                    .padding(horizontal = RouteeTheme.spacing.Spacing32),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(ContentTopPadding))
                Text(
                    text = stringResource(R.string.onboarding_nickname_title),
                    style = RouteeTheme.typography.titleSb20,
                    color = RouteeTheme.colors.white,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(TitleToFieldSpacing))
                RouteeNicknameTextField(
                    state = nicknameState,
                    placeholder = stringResource(R.string.onboarding_nickname_placeholder),
                    guideText = stringResource(R.string.onboarding_nickname_guide),
                    validation = uiState.validation,
                    onKeyboardAction = { focusManager.clearFocus() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { onNicknameFocusChange(it.hasFocus) },
                )
            }
            RouteeCtaPrimaryButton(
                text = stringResource(R.string.onboarding_nickname_start_button),
                onClick = onStartClick,
                enabled = uiState.isStartEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { buttonAreaHeight = with(density) { it.height.toDp() } }
                    .padding(
                        start = RouteeTheme.spacing.Spacing16,
                        top = RouteeTheme.spacing.Spacing16,
                        end = RouteeTheme.spacing.Spacing16,
                        bottom = ButtonBottomPadding,
                    ),
            )
        }
    }
}

@PreviewDeviceSizes
@Composable
private fun NicknameScreenPreview() {
    RouteePreviewTheme {
        NicknameScreen(
            uiState = NicknameUiState(),
            nicknameState = rememberTextFieldState(),
            onBackClick = {},
            onNicknameFocusChange = {},
            onStartClick = {},
        )
    }
}
