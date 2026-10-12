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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.feature.recordedit.model.StickerType

/**
 * 스티커 탭의 스티커 종류 선택 서브탭 (sticker_palette)
 *
 * @param selectedSticker 선택된 스티커 종류. 스티커 탭 진입 시에는 모두 미선택이므로 null
 */
@Composable
internal fun StickerPalette(
    selectedSticker: StickerType?,
    onStickerClick: (StickerType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(RouteeTheme.colors.black80)
            .padding(
                horizontal = RouteeTheme.spacing.Spacing16,
                vertical = RouteeTheme.spacing.Spacing6,
            )
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StickerType.entries.forEach { stickerType ->
            StickerItem(
                stickerType = stickerType,
                isSelected = stickerType == selectedSticker,
                onClick = { onStickerClick(stickerType) },
            )
        }
    }
}

private class StickerPalettePreviewParameterProvider : PreviewParameterProvider<StickerType?> {
    override val values = sequenceOf(null) + StickerType.entries.asSequence()
}

@Preview(showBackground = true, backgroundColor = 0xFF101113, widthDp = 360)
@Composable
private fun StickerPalettePreview(
    @PreviewParameter(StickerPalettePreviewParameterProvider::class) selectedSticker: StickerType?,
) {
    RouteeTheme {
        StickerPalette(
            selectedSticker = selectedSticker,
            onStickerClick = {},
        )
    }
}
