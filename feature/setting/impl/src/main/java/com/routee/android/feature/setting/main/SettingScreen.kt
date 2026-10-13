package com.routee.android.feature.setting.main

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
internal fun SettingRoute(
    onLogoutClick: () -> Unit,
) {
    SettingScreen(
        onLogoutClick = onLogoutClick,
    )
}

@Composable
private fun SettingScreen(
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(RouteeTheme.spacing.Spacing24),
        verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing12),
    ) {
        Text(
            text = "설정",
            style = RouteeTheme.typography.titleSb24,
            color = RouteeTheme.colors.white,
        )
        Button(onClick = onLogoutClick) {
            Text(text = "로그아웃")
        }
    }
}
