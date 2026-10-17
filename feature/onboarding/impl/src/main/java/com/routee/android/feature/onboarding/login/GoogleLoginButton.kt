package com.routee.android.feature.onboarding.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.R as DesignSystemR
import com.routee.android.core.designsystem.extension.noRippleClickable
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.feature.onboarding.R

private val ButtonMinHeight = 54.dp

@Composable
internal fun GoogleLoginButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .defaultMinSize(minHeight = ButtonMinHeight)
            .clip(RoundedCornerShape(RouteeTheme.radius.Full))
            .background(RouteeTheme.colors.white)
            .noRippleClickable(onClick = onClick)
            .semantics { role = Role.Button }
            .padding(horizontal = RouteeTheme.spacing.Spacing16),
        horizontalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing8, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            imageVector = ImageVector.vectorResource(DesignSystemR.drawable.ic_googlelogo),
            contentDescription = null,
        )
        Text(
            text = stringResource(R.string.onboarding_login_google_button),
            style = RouteeTheme.typography.labelSb18,
            color = RouteeTheme.colors.black,
        )
    }
}

@Preview(widthDp = 360)
@Composable
private fun GoogleLoginButtonPreview() {
    RouteePreviewTheme {
        GoogleLoginButton(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(RouteeTheme.spacing.Spacing16),
        )
    }
}
