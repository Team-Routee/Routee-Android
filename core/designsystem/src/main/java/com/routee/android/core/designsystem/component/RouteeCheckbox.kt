package com.routee.android.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.R
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme

private val CheckboxSize = 24.dp

/**
 * 체크박스
 *
 * [onCheckedChange]가 null이면 체크박스 자체는 클릭되지 않는다. 약관 행처럼 행 전체를 눌러 토글할 때는 null을 넘기고 행에 toggleable을 건다
 */
@Composable
fun RouteeCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val toggleModifier = if (onCheckedChange != null) {
        Modifier.toggleable(
            value = checked,
            interactionSource = null,
            indication = null,
            role = Role.Checkbox,
            onValueChange = onCheckedChange,
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .then(toggleModifier)
            .size(CheckboxSize)
            .clip(RoundedCornerShape(RouteeTheme.radius.Radius4))
            .background(if (checked) RouteeTheme.colors.mint300 else RouteeTheme.colors.grey600),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Image(
                painter = painterResource(R.drawable.ic_check_sm_black),
                contentDescription = null,
            )
        }
    }
}

private class RouteeCheckboxPreviewParameterProvider : PreviewParameterProvider<Boolean> {
    override val values = sequenceOf(false, true)
}

@Preview
@Composable
private fun RouteeCheckboxPreview(
    @PreviewParameter(RouteeCheckboxPreviewParameterProvider::class) checked: Boolean,
) {
    RouteePreviewTheme {
        RouteeCheckbox(
            checked = checked,
            onCheckedChange = {},
            modifier = Modifier.padding(RouteeTheme.spacing.Spacing16),
        )
    }
}
