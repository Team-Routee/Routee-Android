package com.routee.android.core.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.routee.android.core.designsystem.preview.PreviewFontSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme

// page_header_lg_android

/**
 * 탭 화면 상단 제목
 */
@Composable
fun RouteePageHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = RouteeTheme.typography.titleSb20,
        color = RouteeTheme.colors.white,
        modifier = modifier
            .fillMaxWidth()
            .padding(RouteeTheme.spacing.Spacing16)
            .semantics { heading() },
    )
}

@PreviewFontSizes
@Composable
private fun RouteePageHeaderPreview() {
    RouteePreviewTheme {
        RouteePageHeader(title = "설정")
    }
}
