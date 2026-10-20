package com.routee.android.feature.workout.component.recording

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.R as DesignSystemR
import com.routee.android.core.designsystem.component.RouteeAutoSizeText
import com.routee.android.core.designsystem.preview.PreviewFontSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.feature.workout.R

private val ButtonSize = 60.dp
private val IconSize = 24.dp

/**
 * 사진 촬영 버튼 (btn_camera_main)
 *
 * @param count    화면에 보일 촬영 개수 (예: "5/20")
 * @param onClick  탭 시 호출
 * @param enabled  false 이면 회색, 탭 무시
 */
@Composable
internal fun CameraButton(
    count: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Column(
        modifier = modifier
            .size(ButtonSize)
            .clip(CircleShape)
            .background(if (enabled) RouteeTheme.colors.mint300 else RouteeTheme.colors.grey200)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(
                top = RouteeTheme.spacing.Spacing6,
                start = RouteeTheme.spacing.Spacing4,
                end = RouteeTheme.spacing.Spacing4,
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            imageVector = ImageVector.vectorResource(DesignSystemR.drawable.ic_camera_fill_black),
            contentDescription = stringResource(R.string.workout_recording_camera_a11y),
            modifier = Modifier.size(IconSize),
        )
        RouteeAutoSizeText(
            text = count,
            style = RouteeTheme.typography.labelSb12,
            color = RouteeTheme.colors.grey600,
            textAlign = TextAlign.Center,
        )
    }
}

@PreviewFontSizes
@Composable
private fun CameraButtonPreview() {
    RouteePreviewTheme(modifier = Modifier.padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CameraButton(count = "5/20", onClick = {})
            CameraButton(count = "20/20", onClick = {}, enabled = false)
        }
    }
}
