package com.routee.android.feature.onboarding.terms

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.routee.android.core.designsystem.R as DesignSystemR
import com.routee.android.core.designsystem.component.RouteeBgEllipse
import com.routee.android.core.designsystem.component.RouteeCheckbox
import com.routee.android.core.designsystem.component.RouteeCtaPrimaryButton
import com.routee.android.core.designsystem.component.RouteePageNavBar
import com.routee.android.core.designsystem.extension.noRippleClickable
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.core.ui.collectSideEffect
import com.routee.android.core.ui.terms.TermsDocument
import com.routee.android.feature.onboarding.R
import com.routee.android.feature.onboarding.terms.model.TermsItem
import com.routee.android.feature.onboarding.terms.model.TermsSideEffect
import com.routee.android.feature.onboarding.terms.model.TermsUiState

private val ContentTopPadding = 113.dp
private val AllAgreeVerticalPadding = 20.dp
private val ButtonBottomPadding = 75.dp

@Composable
internal fun TermsRoute(
    viewModel: TermsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToNickname: (idToken: String, marketingConsent: Boolean) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    viewModel.sideEffect.collectSideEffect { sideEffect ->
        when (sideEffect) {
            TermsSideEffect.NavigateBack -> onNavigateBack()

            is TermsSideEffect.NavigateToNickname -> onNavigateToNickname(
                sideEffect.idToken,
                sideEffect.marketingConsent,
            )
        }
    }

    BackHandler(onBack = viewModel::onBackClick)

    TermsScreen(
        uiState = uiState,
        onBackClick = viewModel::onBackClick,
        onAllCheckedChange = viewModel::onAllCheckedChange,
        onItemCheckedChange = viewModel::onItemCheckedChange,
        onDocumentClick = { document -> uriHandler.openUri(document.url) },
        onNextClick = viewModel::onNextClick,
    )
}

@Composable
private fun TermsScreen(
    uiState: TermsUiState,
    onBackClick: () -> Unit,
    onAllCheckedChange: (Boolean) -> Unit,
    onItemCheckedChange: (TermsItem, Boolean) -> Unit,
    onDocumentClick: (TermsDocument) -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    RouteeBgEllipse(modifier = modifier) {
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
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = RouteeTheme.spacing.Spacing16),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(ContentTopPadding))
                Text(
                    text = stringResource(R.string.onboarding_terms_title),
                    style = RouteeTheme.typography.titleSb20,
                    color = RouteeTheme.colors.white,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(RouteeTheme.spacing.Spacing32))
                Column(verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing8)) {
                    AllAgreeRow(
                        checked = uiState.isAllChecked,
                        onCheckedChange = onAllCheckedChange,
                    )
                    Column {
                        TermsItem.entries.forEach { item ->
                            TermsItemRow(
                                item = item,
                                checked = uiState.isChecked(item),
                                onCheckedChange = { checked -> onItemCheckedChange(item, checked) },
                                onDocumentClick = onDocumentClick,
                            )
                        }
                    }
                }
            }
            RouteeCtaPrimaryButton(
                text = stringResource(R.string.onboarding_terms_next_button),
                onClick = onNextClick,
                enabled = uiState.isNextEnabled,
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

@Composable
private fun AllAgreeRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = RouteeTheme.spacing.Spacing12),
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
                .padding(vertical = AllAgreeVerticalPadding),
            horizontalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RouteeCheckbox(checked = checked, onCheckedChange = null)
            Text(
                text = stringResource(R.string.onboarding_terms_all_agree),
                style = RouteeTheme.typography.labelSb16,
                color = RouteeTheme.colors.white,
            )
        }
        HorizontalDivider(thickness = RouteeTheme.border.Thickness, color = RouteeTheme.colors.grey600)
    }
}

@Composable
private fun TermsItemRow(
    item: TermsItem,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onDocumentClick: (TermsDocument) -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = stringResource(item.titleRes)
    val tag =
        stringResource(if (item.isRequired) R.string.onboarding_terms_required else R.string.onboarding_terms_optional)
    val tagColor = if (item.isRequired) RouteeTheme.colors.mint300 else RouteeTheme.colors.grey50

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .toggleable(
                    value = checked,
                    interactionSource = null,
                    indication = null,
                    role = Role.Checkbox,
                    onValueChange = onCheckedChange,
                )
                .padding(
                    start = RouteeTheme.spacing.Spacing12,
                    top = RouteeTheme.spacing.Spacing12,
                    bottom = RouteeTheme.spacing.Spacing12,
                    end = if (item.document == null) RouteeTheme.spacing.Spacing12 else 0.dp,
                ),
            horizontalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RouteeCheckbox(checked = checked, onCheckedChange = null)
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = tagColor)) { append(tag) }
                    append(" ")
                    append(title)
                },
                style = RouteeTheme.typography.labelR14,
                color = RouteeTheme.colors.grey50,
                modifier = Modifier.weight(1f),
            )
        }
        item.document?.let { document ->
            Image(
                imageVector = ImageVector.vectorResource(DesignSystemR.drawable.ic_chevron_right_sm_white),
                contentDescription = stringResource(R.string.onboarding_terms_document_a11y, title),
                modifier = Modifier
                    .noRippleClickable(role = Role.Button, onClick = { onDocumentClick(document) })
                    .minimumInteractiveComponentSize(),
            )
        }
    }
}

@PreviewScreenSizes
@Composable
private fun TermsScreenPreview() {
    RouteePreviewTheme {
        TermsScreen(
            uiState = TermsUiState(checkedItems = setOf(TermsItem.SERVICE, TermsItem.PRIVACY)),
            onBackClick = {},
            onAllCheckedChange = {},
            onItemCheckedChange = { _, _ -> },
            onDocumentClick = {},
            onNextClick = {},
        )
    }
}
