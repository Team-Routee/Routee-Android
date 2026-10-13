package com.routee.android.feature.archive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.routee.android.core.designsystem.theme.RouteeTheme

@Composable
internal fun ArchiveRoute() {
    ArchiveScreen()
}

@Composable
private fun ArchiveScreen(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(RouteeTheme.spacing.Spacing24),
        verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing12),
    ) {
        Text(
            text = "아카이브",
            style = RouteeTheme.typography.titleSb24,
            color = RouteeTheme.colors.white,
        )
    }
}
