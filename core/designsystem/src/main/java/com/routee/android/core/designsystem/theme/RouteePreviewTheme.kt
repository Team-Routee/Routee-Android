package com.routee.android.core.designsystem.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Preview 전용 테마. [RouteeTheme]을 적용하고 앱 기본 배경색을 깔아준다
 */
@Composable
fun RouteePreviewTheme(content: @Composable () -> Unit) {
    RouteeTheme {
        Box(modifier = Modifier.background(RouteeTheme.colors.bgPrimary)) {
            content()
        }
    }
}
