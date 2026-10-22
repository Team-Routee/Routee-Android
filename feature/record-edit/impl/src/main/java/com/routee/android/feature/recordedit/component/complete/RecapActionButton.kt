package com.routee.android.feature.recordedit.component.complete

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.R as DesignSystemR
import com.routee.android.core.designsystem.extension.noRippleClickable
import com.routee.android.core.designsystem.preview.PreviewFontSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme

private val IconSize = 32.dp
private val IconPadding = 14.dp

/**
 * 리캡 이미지 저장, 공유 버튼 (btn_download_recap, btn_share_recap)
 *
 * @param iconRes             버튼 아이콘
 * @param contentDescription  스크린 리더가 읽을 버튼 설명
 * @param onClick             탭 시 호출
 */
@Composable
internal fun RecapActionButton(
    @DrawableRes iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(RouteeTheme.colors.mint300)
            .noRippleClickable(role = Role.Button, onClick = onClick)
            .padding(IconPadding),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            imageVector = ImageVector.vectorResource(iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(IconSize),
        )
    }
}

@PreviewFontSizes
@Composable
private fun RecapActionButtonPreview() {
    RouteePreviewTheme {
        Row {
            RecapActionButton(
                iconRes = DesignSystemR.drawable.ic_download_recap_lg,
                contentDescription = "",
                onClick = {},
            )
            RecapActionButton(
                iconRes = DesignSystemR.drawable.ic_share_recap_lg,
                contentDescription = "",
                onClick = {},
            )
        }
    }
}
