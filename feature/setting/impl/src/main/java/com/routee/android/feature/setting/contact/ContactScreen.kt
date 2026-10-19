package com.routee.android.feature.setting.contact

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.component.RouteeBgEllipse
import com.routee.android.core.designsystem.component.RouteePageNavBar
import com.routee.android.core.designsystem.component.message.LocalRouteeMessageState
import com.routee.android.core.designsystem.preview.PreviewDeviceSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.core.ui.link.RouteeLink
import com.routee.android.core.ui.link.tryOpenUri
import com.routee.android.feature.setting.R
import com.routee.android.feature.setting.component.contact.ContactButton
import com.routee.android.feature.setting.component.contact.ContactButtonType

private val ContentTopSpacing = 27.dp

@Composable
internal fun ContactRoute(
    onBackClick: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val messageState = LocalRouteeMessageState.current
    val linkErrorText = stringResource(R.string.setting_main_link_error)

    ContactScreen(
        onBackClick = onBackClick,
        onLinkClick = { url -> if (!uriHandler.tryOpenUri(url)) messageState.showToast(text = linkErrorText) },
    )
}

@Composable
private fun ContactScreen(
    onBackClick: () -> Unit,
    onLinkClick: (String) -> Unit,
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
                title = stringResource(R.string.setting_contact_title),
            )
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = RouteeTheme.spacing.Spacing16,
                        top = ContentTopSpacing,
                        end = RouteeTheme.spacing.Spacing16,
                    ),
                verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing32),
            ) {
                ContactGuide()
                ContactButtons(onLinkClick = onLinkClick)
            }
        }
    }
}

@Composable
private fun ContactGuide() {
    Column(verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing8)) {
        Text(
            text = stringResource(R.string.setting_contact_guide_title),
            style = RouteeTheme.typography.titleSb20,
            color = RouteeTheme.colors.white,
        )
        Text(
            text = stringResource(R.string.setting_contact_guide_description),
            style = RouteeTheme.typography.labelR12,
            color = RouteeTheme.colors.grey200,
        )
    }
}

@Composable
private fun ContactButtons(onLinkClick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing8)) {
        ContactButton(
            type = ContactButtonType.KAKAO_TALK,
            onClick = { onLinkClick(RouteeLink.KAKAO_CHANNEL.url) },
            modifier = Modifier.fillMaxWidth(),
        )
        ContactButton(
            type = ContactButtonType.EMAIL,
            onClick = { onLinkClick(RouteeLink.CONTACT_EMAIL.url) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@PreviewDeviceSizes
@Composable
private fun ContactScreenPreview() {
    RouteePreviewTheme {
        ContactScreen(onBackClick = {}, onLinkClick = {})
    }
}
