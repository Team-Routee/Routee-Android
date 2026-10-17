package com.routee.android.feature.workout.component.recording

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.R as DesignSystemR
import com.routee.android.core.designsystem.component.RouteeAsyncImage
import com.routee.android.core.designsystem.component.dialog.RouteeDialogContainer
import com.routee.android.core.designsystem.component.textfield.RouteeBasicTextField
import com.routee.android.core.designsystem.component.textfield.RouteeTextFieldLengthCounter
import com.routee.android.core.designsystem.extension.clearFocus
import com.routee.android.core.designsystem.extension.noRippleClickable
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.feature.workout.R

private const val LOCATION_MAX_LENGTH = 16
private const val PHOTO_ASPECT_RATIO = 272f / 256f
private val LocationFieldMinHeight = 42.dp

/**
 * 사진 타임라인 확인 다이얼로그 (modal_photocheck_android)
 *
 * 사진과 장소명을 보여주고, 장소명은 바로 고칠 수 있다. 입력값은 [locationState]에 바로 반영되고, 저장은 [onDismissRequest]에서 한다
 *
 * @param onDismissRequest X, 딤 영역, 뒤로가기 시 호출
 */
@Composable
internal fun PhotoTimelineDialog(
    imageUrl: String?,
    locationState: TextFieldState,
    onDeleteClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    RouteeDialogContainer(
        onDismissRequest = onDismissRequest,
        contentPadding = PaddingValues(
            start = RouteeTheme.spacing.Spacing24,
            top = RouteeTheme.spacing.Spacing20,
            end = RouteeTheme.spacing.Spacing24,
            bottom = RouteeTheme.spacing.Spacing24,
        ),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.clearFocus(focusManager),
            verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing12),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                PhotoTimelineIconButton(
                    iconRes = DesignSystemR.drawable.ic_delete_lg,
                    contentDescription = stringResource(R.string.workout_recording_photo_delete_a11y),
                    backgroundColor = RouteeTheme.colors.grey900,
                    onClick = onDeleteClick,
                )
                PhotoTimelineIconButton(
                    iconRes = DesignSystemR.drawable.ic_cancel_white_lg,
                    contentDescription = stringResource(R.string.workout_recording_photo_close_a11y),
                    backgroundColor = RouteeTheme.colors.white10,
                    onClick = onDismissRequest,
                )
            }
            PhotoImage(imageUrl = imageUrl)
            LocationNameField(state = locationState)
        }
    }
}

@Composable
private fun PhotoImage(
    imageUrl: String?,
    modifier: Modifier = Modifier,
) {
    RouteeAsyncImage(
        url = imageUrl,
        contentDescription = null,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(PHOTO_ASPECT_RATIO)
            .clip(RoundedCornerShape(RouteeTheme.radius.Radius12)),
    )
}

// modal_location_input_android
// TODO 추후 텍스트필드 박스형 공통 컨포넌트로 묶기 (닉네임 위치 입력칸과 함께)
@Composable
private fun LocationNameField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(RouteeTheme.radius.Radius12)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing2),
        horizontalAlignment = Alignment.End,
    ) {
        RouteeBasicTextField(
            state = state,
            textStyle = RouteeTheme.typography.labelM14,
            placeholder = stringResource(R.string.workout_recording_photo_location_placeholder),
            maxLength = LOCATION_MAX_LENGTH,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            trailingContent = {
                val description = stringResource(R.string.workout_recording_photo_location_edit_a11y)
                Image(
                    imageVector = ImageVector.vectorResource(DesignSystemR.drawable.ic_edit_sm_line_white),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(RouteeTheme.spacing.Spacing2)
                        .semantics { contentDescription = description },
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = LocationFieldMinHeight)
                .background(color = RouteeTheme.colors.dimPrimary, shape = shape)
                .border(width = RouteeTheme.border.Thickness, color = RouteeTheme.colors.white30, shape = shape)
                .padding(horizontal = RouteeTheme.spacing.Spacing12),
        )
        RouteeTextFieldLengthCounter(state = state, maxLength = LOCATION_MAX_LENGTH)
    }
}

@Composable
private fun PhotoTimelineIconButton(
    iconRes: Int,
    contentDescription: String,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Image(
        imageVector = ImageVector.vectorResource(iconRes),
        contentDescription = contentDescription,
        modifier = modifier
            .background(color = backgroundColor, shape = CircleShape)
            .noRippleClickable(role = Role.Button, onClick = onClick),
    )
}

@Preview
@Composable
private fun PhotoTimelineDialogPreview() {
    RouteeTheme {
        PhotoTimelineDialog(
            imageUrl = null,
            locationState = rememberTextFieldState("관악산 말바위 전망 포토 스팟"),
            onDeleteClick = {},
            onDismissRequest = {},
        )
    }
}
