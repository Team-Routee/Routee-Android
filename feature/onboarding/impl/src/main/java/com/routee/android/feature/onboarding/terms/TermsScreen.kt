package com.routee.android.feature.onboarding.terms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.routee.android.core.designsystem.theme.RouteeTheme

@Composable
internal fun TermsRoute(
    onAgreeClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    TermsScreen(
        onAgreeClick = onAgreeClick,
        onBackClick = onBackClick,
    )
}

@Composable
private fun TermsScreen(
    onAgreeClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(RouteeTheme.spacing.Spacing24),
        verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing12),
    ) {
        Text(
            text = "이용약관 동의",
            style = RouteeTheme.typography.titleSb24,
            color = RouteeTheme.colors.white,
        )
        Button(onClick = onAgreeClick) {
            Text(text = "닉네임 설정으로")
        }
        Button(onClick = onBackClick) {
            Text(text = "뒤로")
        }
    }
}
