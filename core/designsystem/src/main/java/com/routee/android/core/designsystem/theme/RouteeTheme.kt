package com.routee.android.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

object RouteeTheme {
    val colors: RouteeColors
        @Composable
        @ReadOnlyComposable
        get() = LocalRouteeColors.current

    val typography: RouteeTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalRouteeTypography.current

    val spacing: RouteeSpacing
        get() = RouteeSpacing

    val radius: RouteeRadius
        get() = RouteeRadius
}

@Composable
fun ProvideRouteeColorsAndTypography(
    colors: RouteeColors,
    typography: RouteeTypography,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalRouteeColors provides colors,
        LocalRouteeTypography provides typography,
        content = content,
    )
}

@Composable
fun RouteeTheme(
    colors: RouteeColors = DefaultRouteeColors,
    typography: RouteeTypography = DefaultRouteeTypography,
    content: @Composable () -> Unit,
) {
    ProvideRouteeColorsAndTypography(colors, typography) {
        MaterialTheme(
            colorScheme = darkColorScheme(
                background = colors.bgPrimary,
                surface = colors.bgPrimary,
            ),
            content = content,
        )
    }
}
