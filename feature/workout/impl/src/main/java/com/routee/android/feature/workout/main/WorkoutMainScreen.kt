package com.routee.android.feature.workout.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.core.map.RouteeNaverMap

@Composable
internal fun WorkoutMainRoute() {
    WorkoutMainScreen()
}

@Composable
private fun WorkoutMainScreen(
    modifier: Modifier = Modifier,
) {
    // TODO #52 운동 메인 UI
    RouteeNaverMap(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(RouteeTheme.spacing.Spacing24),
            verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing12),
        ) {
            Text(
                text = "운동",
                style = RouteeTheme.typography.titleSb24,
                color = RouteeTheme.colors.white,
            )
        }
    }
}
