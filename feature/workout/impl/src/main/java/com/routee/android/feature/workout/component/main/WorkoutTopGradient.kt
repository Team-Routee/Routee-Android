package com.routee.android.feature.workout.component.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.theme.RouteeTheme

private val GradientHeight = 141.dp
private const val SOLID_STOP = 0.1f

/**
 * 지도 상단 그라데이션 (img_top_gra)
 */
@Composable
internal fun WorkoutTopGradient(
    modifier: Modifier = Modifier,
) {
    val color = RouteeTheme.colors.grey900
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(GradientHeight)
            .background(
                Brush.verticalGradient(
                    SOLID_STOP to color,
                    1f to color.copy(alpha = 0f),
                ),
            ),
    )
}
