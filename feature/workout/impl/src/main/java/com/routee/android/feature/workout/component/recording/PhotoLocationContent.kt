package com.routee.android.feature.workout.component.recording

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.component.RouteeAsyncImage
import com.routee.android.core.designsystem.component.RouteeBgEllipse
import com.routee.android.core.designsystem.component.RouteeCtaPrimaryButton
import com.routee.android.core.designsystem.component.RouteePageNavBar
import com.routee.android.core.designsystem.component.textfield.RouteeLocationTextField
import com.routee.android.core.designsystem.extension.clearFocus
import com.routee.android.core.designsystem.extension.imePaddingAbove
import com.routee.android.core.designsystem.extension.scrollToEndOnFocus
import com.routee.android.core.designsystem.preview.PreviewDeviceSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.feature.workout.R

private const val PHOTO_ASPECT_RATIO = 328f / 468f
private val PhotoTopPadding = 29.dp
private val PhotoToFieldSpacing = 19.dp
private val ButtonBottomPadding = 28.dp

/**
 * 촬영 후 위치 입력 (운동_이미지삽입)
 *
 * @param locationState 장소명, 최대 16자
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PhotoLocationContent(
    imageUrl: String,
    locationState: TextFieldState,
    onBackClick: () -> Unit,
    onCompleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current
    val scrollState = rememberScrollState()
    var buttonAreaHeight by remember { mutableStateOf(0.dp) }
    var fieldHeight by remember { mutableStateOf(0.dp) }
    val isImeVisible = WindowInsets.isImeVisible

    // 뒤로가기로 키보드만 닫혀도 테두리 복귀
    LaunchedEffect(isImeVisible) {
        if (!isImeVisible) focusManager.clearFocus()
    }

    RouteeBgEllipse(modifier = modifier.clearFocus(focusManager)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            RouteePageNavBar(onBackClick = onBackClick)
            BoxWithConstraints(modifier = Modifier.weight(1f)) {
                val ratioHeight = (maxWidth - RouteeTheme.spacing.Spacing16 * 2) / PHOTO_ASPECT_RATIO
                val availableHeight = maxHeight - PhotoTopPadding - PhotoToFieldSpacing - fieldHeight
                val photoHeight = minOf(ratioHeight, availableHeight).coerceAtLeast(0.dp)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePaddingAbove(buttonAreaHeight)
                        .scrollToEndOnFocus(scrollState)
                        .verticalScroll(scrollState)
                        .padding(horizontal = RouteeTheme.spacing.Spacing16),
                ) {
                    Spacer(modifier = Modifier.height(PhotoTopPadding))
                    RouteeAsyncImage(
                        url = imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(photoHeight)
                            .clip(RoundedCornerShape(RouteeTheme.radius.Radius12)),
                    )
                    Spacer(modifier = Modifier.height(PhotoToFieldSpacing))
                    RouteeLocationTextField(
                        state = locationState,
                        placeholder = stringResource(R.string.workout_recording_photo_location_placeholder),
                        onKeyboardAction = { focusManager.clearFocus() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onSizeChanged { fieldHeight = with(density) { it.height.toDp() } },
                    )
                }
            }
            RouteeCtaPrimaryButton(
                text = stringResource(R.string.workout_recording_photo_location_complete),
                onClick = {
                    focusManager.clearFocus()
                    onCompleteClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { buttonAreaHeight = with(density) { it.height.toDp() } }
                    .padding(
                        start = RouteeTheme.spacing.Spacing16,
                        top = RouteeTheme.spacing.Spacing8,
                        end = RouteeTheme.spacing.Spacing16,
                        bottom = ButtonBottomPadding,
                    ),
            )
        }
    }
}

@PreviewDeviceSizes
@Composable
private fun PhotoLocationContentPreview() {
    RouteePreviewTheme {
        PhotoLocationContent(
            imageUrl = "",
            locationState = rememberTextFieldState("관악산 말바위 전망 포토 스팟"),
            onBackClick = {},
            onCompleteClick = {},
        )
    }
}
