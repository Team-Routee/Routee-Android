package com.routee.android.feature.onboarding.nickname

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.routee.android.core.designsystem.component.RouteeBgEllipse
import com.routee.android.core.designsystem.component.RouteeCtaPrimaryButton
import com.routee.android.core.designsystem.component.RouteePageNavBar
import com.routee.android.core.designsystem.component.textfield.RouteeNicknameTextField
import com.routee.android.core.designsystem.extension.clearFocus
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.core.ui.collectSideEffect
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

    viewModel.sideEffect.collectSideEffect { sideEffect ->
        when (sideEffect) {
            NicknameSideEffect.NavigateToHome -> onNavigateToHome()
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

    RouteeBgEllipse(modifier = modifier.clearFocus(focusManager)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),
        ) {
            RouteePageNavBar(onBackClick = onBackClick)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
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

@PreviewScreenSizes
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
