package com.routee.android.feature.setting.component.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.R as DesignSystemR
import com.routee.android.core.designsystem.component.RouteeAsyncImage
import com.routee.android.core.designsystem.extension.noRippleClickable
import com.routee.android.core.designsystem.preview.PreviewFontSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.feature.setting.R

private val ProfileImageSize = 113.dp
private val CameraIconSize = 20.dp

/**
 * 프로필 사진 (profile_image)
 *
 * [imageUrl]이 null이면 기본 이미지를 보여준다. 누르면 [onClick]으로 사진 변경을 시작한다
 */
@Composable
internal fun ProfileImage(
    imageUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(R.string.setting_profile_image_change_a11y)

    Box(
        modifier = modifier
            .size(ProfileImageSize)
            .noRippleClickable(onClick = onClick)
            .semantics {
                role = Role.Button
                contentDescription = description
            },
    ) {
        RouteeAsyncImage(
            url = imageUrl,
            contentDescription = null,
            modifier = Modifier
                .size(ProfileImageSize)
                .clip(CircleShape),
            fallback = painterResource(DesignSystemR.drawable.img_profile_default),
        )
        Icon(
            imageVector = ImageVector.vectorResource(DesignSystemR.drawable.ic_camera_fill_black_sm),
            contentDescription = null,
            tint = RouteeTheme.colors.black,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .clip(CircleShape)
                .background(RouteeTheme.colors.mint300)
                .padding(RouteeTheme.spacing.Spacing8)
                .size(CameraIconSize),
        )
    }
}

@PreviewFontSizes
@Composable
private fun ProfileImagePreview() {
    RouteePreviewTheme {
        ProfileImage(imageUrl = null, onClick = {})
    }
}
