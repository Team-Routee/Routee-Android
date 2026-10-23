package com.routee.android.feature.setting.withdraw

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.routee.android.core.designsystem.R as DesignSystemR
import com.routee.android.core.designsystem.component.RouteeBgEllipse
import com.routee.android.core.designsystem.component.RouteeCheckbox
import com.routee.android.core.designsystem.component.RouteeCtaPrimaryButton
import com.routee.android.core.designsystem.component.RouteePageNavBar
import com.routee.android.core.designsystem.component.message.LocalRouteeMessageState
import com.routee.android.core.designsystem.component.message.RouteeMessageDuration
import com.routee.android.core.designsystem.extension.noRippleClickable
import com.routee.android.core.designsystem.preview.PreviewDeviceSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.core.ui.CollectSideEffect
import com.routee.android.feature.setting.R
import com.routee.android.feature.setting.component.contact.tryOpenContactEmail
import com.routee.android.feature.setting.component.contact.tryOpenKakaoChannel
import com.routee.android.feature.setting.withdraw.model.WithdrawSideEffect
import com.routee.android.feature.setting.withdraw.model.WithdrawUiState

private val ContentTopSpacing = 27.dp
private val ContactItemHeight = 50.dp
private val BottomSpacing = 28.dp

@Composable
internal fun WithdrawRoute(
    onBackClick: () -> Unit,
    onWithdrawn: () -> Unit,
    viewModel: WithdrawViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    val messageState = LocalRouteeMessageState.current
    val linkErrorText = stringResource(R.string.setting_main_link_error)
    val networkErrorText = stringResource(R.string.setting_network_error_toast)
    val unknownErrorText = stringResource(R.string.setting_unknown_error_toast)

    viewModel.sideEffect.CollectSideEffect { sideEffect ->
        when (sideEffect) {
            WithdrawSideEffect.NavigateToLogin -> onWithdrawn()

            WithdrawSideEffect.ShowNetworkErrorToast -> messageState.showToast(
                text = networkErrorText,
                duration = RouteeMessageDuration.LONG,
            )

            WithdrawSideEffect.ShowUnknownErrorToast -> messageState.showToast(text = unknownErrorText)
        }
    }

    WithdrawScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onKakaoClick = { if (!uriHandler.tryOpenKakaoChannel()) messageState.showToast(text = linkErrorText) },
        onEmailClick = { if (!uriHandler.tryOpenContactEmail()) messageState.showToast(text = linkErrorText) },
        onAgreeChange = viewModel::onAgreeChange,
        onWithdrawClick = viewModel::onWithdrawClick,
    )
}

@Composable
private fun WithdrawScreen(
    uiState: WithdrawUiState,
    onBackClick: () -> Unit,
    onKakaoClick: () -> Unit,
    onEmailClick: () -> Unit,
    onAgreeChange: (Boolean) -> Unit,
    onWithdrawClick: () -> Unit,
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
                title = stringResource(R.string.setting_withdraw_title),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = RouteeTheme.spacing.Spacing16,
                        top = ContentTopSpacing,
                        end = RouteeTheme.spacing.Spacing16,
                        bottom = RouteeTheme.spacing.Spacing24,
                    ),
                verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing18),
            ) {
                Text(
                    text = stringResource(R.string.setting_withdraw_heading),
                    style = RouteeTheme.typography.titleSb20,
                    color = RouteeTheme.colors.white,
                )
                Column(verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing12)) {
                    WarningCard()
                    ContactCard(onKakaoClick = onKakaoClick, onEmailClick = onEmailClick)
                }
            }
            Column(
                modifier = Modifier.padding(
                    start = RouteeTheme.spacing.Spacing16,
                    end = RouteeTheme.spacing.Spacing16,
                    bottom = BottomSpacing,
                ),
                verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing24),
            ) {
                AgreeRow(checked = uiState.isAgreed, onCheckedChange = onAgreeChange)
                RouteeCtaPrimaryButton(
                    text = stringResource(R.string.setting_withdraw_button),
                    onClick = onWithdrawClick,
                    enabled = uiState.isAgreed,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun WarningCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = RouteeTheme.colors.grey900, shape = RoundedCornerShape(RouteeTheme.radius.Radius12))
            .padding(RouteeTheme.spacing.Spacing18),
        verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing14),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing6),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                imageVector = ImageVector.vectorResource(DesignSystemR.drawable.ic_error),
                contentDescription = null,
            )
            Text(
                text = stringResource(R.string.setting_withdraw_warning_title),
                style = RouteeTheme.typography.labelM14,
                color = RouteeTheme.colors.statusError,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing8)) {
            Text(
                text = stringResource(R.string.setting_withdraw_warning_data),
                style = RouteeTheme.typography.labelM12,
                color = RouteeTheme.colors.grey200,
            )
            Text(
                text = stringResource(R.string.setting_withdraw_warning_return),
                style = RouteeTheme.typography.labelM12,
                color = RouteeTheme.colors.grey200,
            )
        }
    }
}

@Composable
private fun ContactCard(
    onKakaoClick: () -> Unit,
    onEmailClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = RouteeTheme.colors.grey900, shape = RoundedCornerShape(RouteeTheme.radius.Radius12))
            .padding(
                start = RouteeTheme.spacing.Spacing18,
                top = RouteeTheme.spacing.Spacing18,
                end = RouteeTheme.spacing.Spacing18,
                bottom = RouteeTheme.spacing.Spacing6,
            ),
        verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing8),
    ) {
        Text(
            text = stringResource(R.string.setting_withdraw_contact_title),
            style = RouteeTheme.typography.labelM14,
            color = RouteeTheme.colors.white,
        )
        Column {
            ContactItem(
                iconRes = DesignSystemR.drawable.ic_kakao_circle,
                label = stringResource(R.string.setting_contact_button_kakao),
                onClick = onKakaoClick,
            )
            HorizontalDivider(thickness = RouteeTheme.border.Thickness, color = RouteeTheme.colors.grey600)
            ContactItem(
                iconRes = DesignSystemR.drawable.ic_email_circle,
                label = stringResource(R.string.setting_contact_button_email),
                onClick = onEmailClick,
            )
        }
    }
}

// usage_list
@Composable
private fun ContactItem(
    @DrawableRes iconRes: Int,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = ContactItemHeight)
            .noRippleClickable(role = Role.Button, onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(imageVector = ImageVector.vectorResource(iconRes), contentDescription = null)
        Text(
            text = label,
            style = RouteeTheme.typography.labelM14,
            color = RouteeTheme.colors.white,
            modifier = Modifier.weight(1f),
        )
        Image(
            imageVector = ImageVector.vectorResource(DesignSystemR.drawable.ic_chevron_right_sm_white),
            contentDescription = null,
        )
    }
}

@Composable
private fun AgreeRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                interactionSource = null,
                indication = null,
                role = Role.Checkbox,
                onValueChange = onCheckedChange,
            )
            .padding(horizontal = RouteeTheme.spacing.Spacing10),
        horizontalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RouteeCheckbox(checked = checked, onCheckedChange = null)
        Text(
            text = stringResource(R.string.setting_withdraw_agree),
            style = RouteeTheme.typography.labelR14,
            color = RouteeTheme.colors.white,
        )
    }
}

private class WithdrawUiStatePreviewParameterProvider : PreviewParameterProvider<WithdrawUiState> {
    override val values = sequenceOf(WithdrawUiState(), WithdrawUiState(isAgreed = true))
}

@PreviewDeviceSizes
@Composable
private fun WithdrawScreenPreview(
    @PreviewParameter(WithdrawUiStatePreviewParameterProvider::class) uiState: WithdrawUiState,
) {
    RouteePreviewTheme {
        WithdrawScreen(
            uiState = uiState,
            onBackClick = {},
            onKakaoClick = {},
            onEmailClick = {},
            onAgreeChange = {},
            onWithdrawClick = {},
        )
    }
}
