package com.routee.android.feature.recordedit.component.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.routee.android.core.designsystem.preview.PreviewFontSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.feature.recordedit.model.RecapColor

/**
 * 기록 편집 색상 팔레트 (color_palette)
 *
 * @param selectedColor 선택된 색상. 최초 진입 시 [RecapColor.DEFAULT]
 */
@Composable
internal fun ColorPalette(
    selectedColor: RecapColor,
    onColorClick: (RecapColor) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(RouteeTheme.colors.black60)
            .padding(
                horizontal = RouteeTheme.spacing.Spacing14,
                vertical = RouteeTheme.spacing.Spacing6,
            )
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(
            space = RouteeTheme.spacing.Spacing6,
            alignment = Alignment.CenterHorizontally,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RecapColor.entries.forEach { recapColor ->
            ColorChip(
                recapColor = recapColor,
                isSelected = recapColor == selectedColor,
                onClick = { onColorClick(recapColor) },
                modifier = Modifier.weight(1f, fill = false),
            )
        }
    }
}

private class ColorPalettePreviewParameterProvider : PreviewParameterProvider<RecapColor> {
    override val values = sequenceOf(RecapColor.DEFAULT, RecapColor.ORANGE, RecapColor.NAVY)
}

@PreviewFontSizes
@Composable
private fun ColorPalettePreview(
    @PreviewParameter(ColorPalettePreviewParameterProvider::class) selectedColor: RecapColor,
) {
    RouteePreviewTheme {
        ColorPalette(
            selectedColor = selectedColor,
            onColorClick = {},
        )
    }
}
