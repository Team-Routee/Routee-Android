package com.routee.android.core.designsystem.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp

internal data class ColorGroup(
    val title: String,
    val colors: List<Pair<String, Brush>>,
)

internal class ColorGroupPreviewParameterProvider : PreviewParameterProvider<ColorGroup> {
    override val values: Sequence<ColorGroup> = with(DefaultRouteeColors) {
        sequenceOf(
            ColorGroup(
                title = "Primitive",
                colors = listOf(
                    "mint100" to SolidColor(mint100),
                    "mint200" to SolidColor(mint200),
                    "mint300" to SolidColor(mint300),
                    "mint400" to SolidColor(mint400),
                    "mint500" to SolidColor(mint500),
                    "mint600" to SolidColor(mint600),
                    "mint700" to SolidColor(mint700),
                    "mint800" to SolidColor(mint800),
                    "mint900" to SolidColor(mint900),
                    "lime100" to SolidColor(lime100),
                    "lime200" to SolidColor(lime200),
                    "lime300" to SolidColor(lime300),
                    "lime400" to SolidColor(lime400),
                    "grey50" to SolidColor(grey50),
                    "grey200" to SolidColor(grey200),
                    "grey300" to SolidColor(grey300),
                    "grey400" to SolidColor(grey400),
                    "grey500" to SolidColor(grey500),
                    "grey600" to SolidColor(grey600),
                    "grey800" to SolidColor(grey800),
                    "grey900" to SolidColor(grey900),
                    "white" to SolidColor(white),
                    "black" to SolidColor(black),
                    "white60" to SolidColor(white60),
                    "white30" to SolidColor(white30),
                    "white10" to SolidColor(white10),
                    "black80" to SolidColor(black80),
                    "black60" to SolidColor(black60),
                    "black50" to SolidColor(black50),
                    "black40" to SolidColor(black40),
                    "black30" to SolidColor(black30),
                ),
            ),
            ColorGroup(
                title = "Semantic",
                colors = listOf(
                    "brandPrimary" to SolidColor(brandPrimary),
                    "brandSecondary" to SolidColor(brandSecondary),
                    "bgPrimary" to SolidColor(bgPrimary),
                    "surfaceTinted" to SolidColor(surfaceTinted),
                    "dimPrimary" to SolidColor(dimPrimary),
                    "dimSecondary" to SolidColor(dimSecondary),
                    "statusError" to SolidColor(statusError),
                    "statusWarning" to SolidColor(statusWarning),
                    "statusSuccess" to SolidColor(statusSuccess),
                    "statusInfo" to SolidColor(statusInfo),
                ),
            ),
            ColorGroup(
                title = "Component",
                colors = listOf(
                    "bgCtaPrimary" to SolidColor(bgCtaPrimary),
                    "bgCtaSecondary" to SolidColor(bgCtaSecondary),
                    "recapOrange" to SolidColor(recapOrange),
                    "recapLime" to SolidColor(recapLime),
                    "recapGreen" to SolidColor(recapGreen),
                    "recapMint" to SolidColor(recapMint),
                    "recapPurple" to SolidColor(recapPurple),
                    "recapPink" to SolidColor(recapPink),
                    "recapWhite" to SolidColor(recapWhite),
                    "recapNavy" to SolidColor(recapNavy),
                    "bgHighlight" to bgHighlight,
                ),
            ),
        )
    }
}

@Preview(widthDp = 480, showBackground = true, backgroundColor = 0xFF101113)
@Composable
private fun RouteeColorsPreview(
    @PreviewParameter(ColorGroupPreviewParameterProvider::class) group: ColorGroup,
) {
    RouteeTheme {
        Column(
            modifier = Modifier
                .background(RouteeTheme.colors.bgPrimary)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = group.title,
                style = RouteeTheme.typography.titleSb20,
                color = RouteeTheme.colors.mint100,
            )
            group.colors.chunked(4).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { (name, brush) -> ColorPreviewItem(name, brush) }
                }
            }
        }
    }
}

@Composable
private fun ColorPreviewItem(name: String, brush: Brush) = Column(
    modifier = Modifier.width(96.dp),
    verticalArrangement = Arrangement.spacedBy(4.dp),
) {
    Box(
        modifier = Modifier
            .width(96.dp)
            .height(48.dp)
            .background(brush, RoundedCornerShape(8.dp)),
    )
    Text(
        text = name,
        style = RouteeTheme.typography.labelR12,
        color = RouteeTheme.colors.white60,
        maxLines = 1,
    )
}
