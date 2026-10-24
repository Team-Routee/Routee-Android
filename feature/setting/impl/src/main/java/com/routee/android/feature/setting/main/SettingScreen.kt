package com.routee.android.feature.setting.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.routee.android.core.common.extension.appVersionName
import com.routee.android.core.designsystem.component.RouteeBgEllipse
import com.routee.android.core.designsystem.component.RouteePageHeader
import com.routee.android.core.designsystem.component.dialog.RouteeTwoButtonDialog
import com.routee.android.core.designsystem.component.message.LocalRouteeMessageState
import com.routee.android.core.designsystem.component.routeeNavBarPadding
import com.routee.android.core.designsystem.preview.PreviewDeviceSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.core.ui.CollectSideEffect
import com.routee.android.core.ui.link.RouteeLink
import com.routee.android.core.ui.link.tryOpenUri
import com.routee.android.core.ui.link.tryOpenUriInNativeApp
import com.routee.android.core.ui.loading.RouteeLoadingOverlay
import com.routee.android.feature.setting.R
import com.routee.android.feature.setting.component.main.SettingMenuItem
import com.routee.android.feature.setting.component.main.SettingSection
import com.routee.android.feature.setting.main.model.SettingSideEffect
import com.routee.android.feature.setting.main.model.SettingUiState

private val HeaderBottomSpacing = 17.dp
private val SectionSpacing = 15.dp
private val ContentBottomSpacing = 44.dp

@Composable
internal fun SettingRoute(
    onProfileClick: () -> Unit,
    onContactClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    onLogout: () -> Unit,
    viewModel: SettingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    val messageState = LocalRouteeMessageState.current
    val linkErrorText = stringResource(R.string.setting_main_link_error)
    val unknownErrorText = stringResource(R.string.setting_unknown_error_toast)
    val context = LocalContext.current
    val appVersion = remember(context) { context.appVersionName }

    viewModel.sideEffect.CollectSideEffect { sideEffect ->
        when (sideEffect) {
            SettingSideEffect.NavigateToLogin -> onLogout()
            SettingSideEffect.ShowUnknownErrorToast -> messageState.showToast(text = unknownErrorText)
        }
    }

    // 요청 중 뒤로가기로 앱을 나가면 서버 로그아웃 뒤 기기 정리가 취소될 수 있음
    BackHandler(enabled = uiState.isLoggingOut) {}

    SettingScreen(
        uiState = uiState,
        appVersion = appVersion,
        onProfileClick = onProfileClick,
        onContactClick = onContactClick,
        onLinkClick = { url -> if (!uriHandler.tryOpenUri(url)) messageState.showToast(text = linkErrorText) },
        onInstagramClick = {
            val url = RouteeLink.INSTAGRAM.url
            val opened = context.tryOpenUriInNativeApp(url) || uriHandler.tryOpenUri(url)
            if (!opened) messageState.showToast(text = linkErrorText)
        },
        onLogoutClick = viewModel::onLogoutClick,
        onLogoutConfirm = viewModel::onLogoutConfirm,
        onLogoutDismiss = viewModel::onLogoutDismiss,
        onWithdrawClick = onWithdrawClick,
    )

    if (uiState.isLoggingOut) RouteeLoadingOverlay()
}

@Composable
private fun SettingScreen(
    uiState: SettingUiState,
    appVersion: String,
    onProfileClick: () -> Unit,
    onContactClick: () -> Unit,
    onLinkClick: (String) -> Unit,
    onInstagramClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onLogoutConfirm: () -> Unit,
    onLogoutDismiss: () -> Unit,
    onWithdrawClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    RouteeBgEllipse(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            RouteePageHeader(title = stringResource(R.string.setting_main_title))
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Spacer(modifier = Modifier.height(HeaderBottomSpacing))
                Column(
                    modifier = Modifier.padding(horizontal = RouteeTheme.spacing.Spacing16),
                    verticalArrangement = Arrangement.spacedBy(SectionSpacing),
                ) {
                    SettingSection(title = stringResource(R.string.setting_main_section_use)) {
                        SettingMenuItem(
                            title = stringResource(R.string.setting_main_menu_profile),
                            onClick = onProfileClick,
                        )
                        SettingMenuItem(
                            title = stringResource(R.string.setting_main_menu_contact),
                            onClick = onContactClick,
                        )
                        SettingMenuItem(
                            title = stringResource(R.string.setting_main_menu_instagram),
                            onClick = onInstagramClick,
                        )
                    }
                    SettingSection(title = stringResource(R.string.setting_main_section_policy)) {
                        SettingMenuItem(
                            title = stringResource(R.string.setting_main_menu_terms),
                            onClick = { onLinkClick(RouteeLink.SERVICE_TERMS.url) },
                        )
                        SettingMenuItem(
                            title = stringResource(R.string.setting_main_menu_privacy),
                            onClick = { onLinkClick(RouteeLink.PRIVACY_POLICY.url) },
                        )
                        SettingMenuItem(
                            title = stringResource(R.string.setting_main_menu_location_terms),
                            onClick = { onLinkClick(RouteeLink.LOCATION_TERMS.url) },
                        )
                    }
                    SettingSection(title = stringResource(R.string.setting_main_section_app_info)) {
                        // TODO 출시 후 탭 시 구글플레이 상세로 이동, 화살표 표시
                        SettingMenuItem(
                            title = stringResource(R.string.setting_main_menu_version),
                            onClick = null,
                            trailingText = appVersion,
                        )
                        SettingMenuItem(
                            title = stringResource(R.string.setting_main_menu_logout),
                            onClick = onLogoutClick,
                        )
                        SettingMenuItem(
                            title = stringResource(R.string.setting_main_menu_withdraw),
                            onClick = onWithdrawClick,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(routeeNavBarPadding(ContentBottomSpacing)))
            }
        }
    }

    if (uiState.isLogoutDialogVisible) {
        RouteeTwoButtonDialog(
            title = stringResource(R.string.setting_main_logout_dialog_title),
            leftButtonText = stringResource(R.string.setting_main_logout_dialog_cancel),
            rightButtonText = stringResource(R.string.setting_main_logout_dialog_confirm),
            onRightButtonClick = onLogoutConfirm,
            onDismissRequest = onLogoutDismiss,
        )
    }
}

@PreviewDeviceSizes
@Composable
private fun SettingScreenPreview() {
    RouteePreviewTheme {
        SettingScreen(
            uiState = SettingUiState(),
            appVersion = "1.0.0",
            onProfileClick = {},
            onContactClick = {},
            onLinkClick = {},
            onInstagramClick = {},
            onLogoutClick = {},
            onLogoutConfirm = {},
            onLogoutDismiss = {},
            onWithdrawClick = {},
        )
    }
}
