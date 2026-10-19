package com.routee.android.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.R
import com.routee.android.core.designsystem.preview.PreviewFontSizes
import com.routee.android.core.designsystem.theme.RouteeTheme

/**
 * 사진 저장 상태를 나타내는 버튼
 *
 * [isSaved]가 true이면 완료 아이콘을 표시한다.
 */
@Composable
fun RouteePhotoSaveButton(
    isSaved: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(RouteeTheme.radius.Radius8))
            .background(RouteeTheme.colors.black80)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            imageVector = ImageVector.vectorResource(
                if (isSaved) R.drawable.ic_success_white else R.drawable.ic_download,
            ),
            contentDescription = stringResource(
                if (isSaved) R.string.designsystem_photo_saved_a11y else R.string.designsystem_photo_save_a11y,
            ),
        )
    }
}

@PreviewFontSizes
@Composable
private fun RouteePhotoSaveButtonPreview() {
    RouteeTheme {
        Column {
            RouteePhotoSaveButton(isSaved = false, onClick = {})
            RouteePhotoSaveButton(isSaved = true, onClick = {})
        }
    }
}
