package com.routee.android.feature.recordedit.component.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.extension.noRippleSelectable
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.feature.recordedit.model.RecapColor
import com.routee.android.feature.recordedit.model.color

private val ChipMaxSize = 36.dp
private val SelectedRingThickness = 2.dp
private val SwatchBorderThickness = 1.dp

/**
 * 색상 팔레트의 색상 칩 (color_chip)
 */
@Composable
internal fun ColorChip(
    recapColor: RecapColor,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorName = stringResource(recapColor.nameRes)

    Box(
        modifier = modifier
            .widthIn(max = ChipMaxSize)
            .aspectRatio(1f)
            .clip(CircleShape)
            .border(
                width = SelectedRingThickness,
                color = if (isSelected) RouteeTheme.colors.white else Color.Transparent,
                shape = CircleShape,
            )
            .noRippleSelectable(selected = isSelected, role = Role.RadioButton, onClick = onClick)
            .semantics { contentDescription = colorName }
            .padding(RouteeTheme.spacing.Spacing4)
            .clip(CircleShape)
            .background(recapColor.color)
            .border(
                width = SwatchBorderThickness,
                color = RouteeTheme.colors.white,
                shape = CircleShape,
            ),
    )
}

private class ColorChipPreviewParameterProvider : PreviewParameterProvider<Boolean> {
    override val values = sequenceOf(false, true)
}

@Preview
@Composable
private fun ColorChipPreview(
    @PreviewParameter(ColorChipPreviewParameterProvider::class) isSelected: Boolean,
) {
    RouteePreviewTheme {
        ColorChip(
            recapColor = RecapColor.ORANGE,
            isSelected = isSelected,
            onClick = {},
        )
    }
}
