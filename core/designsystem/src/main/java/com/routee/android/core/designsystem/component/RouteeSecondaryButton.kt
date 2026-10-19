package com.routee.android.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.R
import com.routee.android.core.designsystem.preview.PreviewFontSizes
import com.routee.android.core.designsystem.theme.RouteeTheme

/**
 * 플러스 아이콘과 문구가 있는 보조 버튼
 *
 * 폭은 호출하는 화면에서 [modifier]로 지정한다.
 */
@Composable
fun RouteeSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(RouteeTheme.radius.Radius12)
    Row(
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .clip(shape)
            .background(RouteeTheme.colors.bgCtaSecondary)
            .border(1.dp, RouteeTheme.colors.mint300, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = RouteeTheme.spacing.Spacing12),
        horizontalArrangement = Arrangement.spacedBy(
            RouteeTheme.spacing.Spacing6,
            Alignment.CenterHorizontally,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            imageVector = ImageVector.vectorResource(R.drawable.ic_plus_circle_sm_mint),
            contentDescription = null,
        )
        Text(
            text = text,
            color = RouteeTheme.colors.white,
            style = RouteeTheme.typography.labelSb14,
            modifier = Modifier.weight(1f, fill = false),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@PreviewFontSizes
@Composable
private fun RouteeSecondaryButtonPreview() {
    RouteeTheme {
        RouteeSecondaryButton(
            text = "지점 추가",
            onClick = {},
            modifier = Modifier.width(328.dp),
        )
    }
}
