package com.routee.android.feature.onboarding.login

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.routee.android.core.designsystem.R as DesignSystemR
import com.routee.android.core.designsystem.component.dialog.RouteeOneButtonDialog
import com.routee.android.core.designsystem.component.message.LocalRouteeMessageState
import com.routee.android.core.designsystem.component.message.RouteeMessageDuration
import com.routee.android.core.designsystem.component.message.routeeMessageAnchor
import com.routee.android.core.designsystem.preview.PreviewDeviceSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.core.ui.CollectSideEffect
import com.routee.android.core.ui.link.RouteeLink
import com.routee.android.feature.onboarding.R
import com.routee.android.feature.onboarding.login.model.LoginSideEffect
import com.routee.android.feature.onboarding.login.model.LoginUiState
import kotlinx.coroutines.launch

private const val BACKGROUND_HEIGHT = 639f
private const val LOGIN_BACKGROUND_RATIO = 360f / BACKGROUND_HEIGHT
private const val LOGO_TOP_IN_BACKGROUND = 311f
private const val LOGO_HEIGHT = 76f
private val BottomPadding = 25.dp
private val LogoAlignment = BiasAlignment(
    horizontalBias = 0f,
    verticalBias = 2 * LOGO_TOP_IN_BACKGROUND / (BACKGROUND_HEIGHT - LOGO_HEIGHT) - 1,
)

@Composable
internal fun LoginRoute(
    viewModel: LoginViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToTerms: (idToken: String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalActivity.current
    val messageState = LocalRouteeMessageState.current
    val coroutineScope = rememberCoroutineScope()
    val exitMessage = stringResource(R.string.onboarding_login_exit_toast)
    val networkErrorMessage = stringResource(R.string.onboarding_network_error_toast)
    val unknownErrorMessage = stringResource(R.string.onboarding_unknown_error_toast)
    var lastBackPressedAt by remember { mutableLongStateOf(0L) }
    var isRequestingIdToken by remember { mutableStateOf(false) }

    viewModel.sideEffect.CollectSideEffect { sideEffect ->
        when (sideEffect) {
            LoginSideEffect.NavigateToHome -> onNavigateToHome()

            is LoginSideEffect.NavigateToTerms -> onNavigateToTerms(sideEffect.idToken)

            LoginSideEffect.ShowNetworkErrorToast -> messageState.showToast(
                text = networkErrorMessage,
                duration = RouteeMessageDuration.SHORT,
            )

            LoginSideEffect.ShowUnknownErrorToast -> messageState.showToast(
                text = unknownErrorMessage,
                duration = RouteeMessageDuration.LONG,
            )
        }
    }

    BackHandler {
        val now = SystemClock.elapsedRealtime()
        if (now - lastBackPressedAt < RouteeMessageDuration.SHORT.millis) {
            activity?.finish()
        } else {
            lastBackPressedAt = now
            messageState.showToast(text = exitMessage)
        }
    }

    LoginScreen(
        uiState = uiState,
        onSessionExpiredDialogDismiss = viewModel::onSessionExpiredDialogDismiss,
        onGoogleLoginClick = {
            if (activity != null && !isRequestingIdToken) {
                isRequestingIdToken = true
                coroutineScope.launch {
                    viewModel.onIdTokenResult(activity.requestGoogleIdToken())
                    isRequestingIdToken = false
                }
            }
        },
    )
}

@Composable
private fun LoginScreen(
    uiState: LoginUiState,
    onSessionExpiredDialogDismiss: () -> Unit,
    onGoogleLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(RouteeTheme.colors.bgPrimary),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(LOGIN_BACKGROUND_RATIO),
        ) {
            Image(
                painter = painterResource(DesignSystemR.drawable.img_bg_login),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Image(
                imageVector = ImageVector.vectorResource(DesignSystemR.drawable.routee_signature_gradient_md),
                contentDescription = null,
                modifier = Modifier.align(LogoAlignment),
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    start = RouteeTheme.spacing.Spacing16,
                    end = RouteeTheme.spacing.Spacing16,
                    bottom = BottomPadding,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing24),
        ) {
            GoogleLoginButton(
                onClick = onGoogleLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .routeeMessageAnchor(),
            )
            TermsNoticeText(modifier = Modifier.fillMaxWidth())
        }
    }

    if (uiState.isSessionExpiredDialogVisible) {
        RouteeOneButtonDialog(
            title = stringResource(R.string.onboarding_login_session_expired_dialog_title),
            description = stringResource(R.string.onboarding_login_session_expired_dialog_description),
            buttonText = stringResource(R.string.onboarding_login_session_expired_dialog_confirm),
            onButtonClick = onSessionExpiredDialogDismiss,
            onDismissRequest = onSessionExpiredDialogDismiss,
        )
    }
}

@Composable
private fun TermsNoticeText(modifier: Modifier = Modifier) {
    val html = stringResource(
        R.string.onboarding_login_terms_notice,
        RouteeLink.SERVICE_TERMS.url,
        RouteeLink.PRIVACY_POLICY.url,
    )
    val text = remember(html) {
        AnnotatedString.fromHtml(
            htmlString = html,
            linkStyles = TextLinkStyles(SpanStyle(textDecoration = TextDecoration.Underline)),
        )
    }

    Text(
        text = text,
        style = RouteeTheme.typography.labelR10,
        color = RouteeTheme.colors.white60,
        textAlign = TextAlign.Center,
        modifier = modifier,
    )
}

private class LoginUiStatePreviewParameterProvider : PreviewParameterProvider<LoginUiState> {
    override val values = sequenceOf(
        LoginUiState(),
        LoginUiState(isSessionExpiredDialogVisible = true),
    )
}

@PreviewDeviceSizes
@Composable
private fun LoginScreenPreview(
    @PreviewParameter(LoginUiStatePreviewParameterProvider::class) uiState: LoginUiState,
) {
    RouteePreviewTheme {
        LoginScreen(
            uiState = uiState,
            onSessionExpiredDialogDismiss = {},
            onGoogleLoginClick = {},
        )
    }
}
