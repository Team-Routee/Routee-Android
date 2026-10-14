package com.routee.android.feature.onboarding.nickname

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
internal fun NicknameRoute(
    onStartClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    NicknameScreen(
        onStartClick = onStartClick,
        onBackClick = onBackClick,
    )
}

@Composable
private fun NicknameScreen(
    onStartClick: () -> Unit,
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
            text = "닉네임 설정",
            style = RouteeTheme.typography.titleSb24,
            color = RouteeTheme.colors.white,
        )
        Button(onClick = onStartClick) {
            Text(text = "시작하기")
        }
        Button(onClick = onBackClick) {
            Text(text = "뒤로")
        }
    }
}
