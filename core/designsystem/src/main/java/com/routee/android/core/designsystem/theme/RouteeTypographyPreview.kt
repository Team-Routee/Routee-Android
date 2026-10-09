package com.routee.android.core.designsystem.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(
    name = "Routee Typography",
    widthDp = 480,
    heightDp = 1040,
)
@Composable
private fun RouteeTypographyPreview() {
    RouteeTheme {
        val typography = RouteeTheme.typography
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(RouteeTheme.colors.bgPrimary)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Typography System",
                style = typography.titleSb24,
                color = RouteeTheme.colors.mint100,
            )
            listOf(
                "display_52" to typography.display52,
                "display_40" to typography.display40,
                "display_30" to typography.display30,
                "display_26" to typography.display26,
                "title_sb_24" to typography.titleSb24,
                "title_sb_20" to typography.titleSb20,
                "title_sb_18" to typography.titleSb18,
                "body_r_16" to typography.bodyR16,
                "body_sb_14" to typography.bodySb14,
                "label_sb_18" to typography.labelSb18,
                "label_sb_16" to typography.labelSb16,
                "label_m_16" to typography.labelM16,
                "label_sb_14" to typography.labelSb14,
                "label_m_14" to typography.labelM14,
                "label_r_14" to typography.labelR14,
                "label_sb_12" to typography.labelSb12,
                "label_m_12" to typography.labelM12,
                "label_r_12" to typography.labelR12,
            ).forEach { (name, style) ->
                val sample = if (name.startsWith("display")) "Routee Style Guide" else "루티 스타일 가이드"
                Text(
                    text = "$name  $sample",
                    style = style,
                    color = RouteeTheme.colors.white,
                )
            }
        }
    }
}
