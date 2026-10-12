package com.routee.android.feature.recordedit.component.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.routee.android.core.designsystem.extension.noRippleClickable
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.feature.recordedit.model.StickerType

/**
 * 스티커 팔레트의 스티커 종류 항목 (sticker_item)
 */
@Composable
internal fun StickerItem(
    stickerType: StickerType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = RouteeTheme.spacing

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(RouteeTheme.radius.Radius8))
            .background(if (isSelected) RouteeTheme.colors.white10 else Color.Transparent)
            .noRippleClickable(onClick = onClick)
            .semantics {
                role = Role.RadioButton
                selected = isSelected
            }
            .padding(
                PaddingValues(
                    start = spacing.Spacing8,
                    top = spacing.Spacing6,
                    end = spacing.Spacing12,
                    bottom = spacing.Spacing6,
                ),
            ),
        horizontalArrangement = Arrangement.spacedBy(spacing.Spacing4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(
                if (isSelected) stickerType.selectedIconRes else stickerType.unselectedIconRes,
            ),
            contentDescription = null,
            tint = Color.Unspecified,
        )
        Text(
            text = stringResource(stickerType.labelRes),
            style = RouteeTheme.typography.labelM12,
            color = if (isSelected) RouteeTheme.colors.white else RouteeTheme.colors.grey200,
            maxLines = 1,
        )
    }
}

private class StickerItemPreviewParameterProvider : PreviewParameterProvider<Boolean> {
    override val values = sequenceOf(false, true)
}

@Preview(showBackground = true, backgroundColor = 0xFF101113)
@Composable
private fun StickerItemPreview(
    @PreviewParameter(StickerItemPreviewParameterProvider::class) isSelected: Boolean,
) {
    RouteeTheme {
        StickerItem(
            stickerType = StickerType.PHOTO_TIMELINE,
            isSelected = isSelected,
            onClick = {},
        )
    }
}
