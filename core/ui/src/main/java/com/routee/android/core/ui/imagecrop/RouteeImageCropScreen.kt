package com.routee.android.core.ui.imagecrop

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.routee.android.core.designsystem.component.RouteeBgEllipse
import com.routee.android.core.designsystem.component.RouteeCropAspectRatio
import com.routee.android.core.designsystem.component.RouteeImageCropper
import com.routee.android.core.designsystem.component.RouteeImageRotationController
import com.routee.android.core.designsystem.component.RouteePageNavBar
import com.routee.android.core.designsystem.component.rememberRouteeImageCropperState
import com.routee.android.core.designsystem.extension.noRippleClickable
import com.routee.android.core.designsystem.preview.PreviewDeviceSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.core.ui.R
import kotlinx.coroutines.launch

/**
 * 이미지 자르기 화면
 *
 * 비율만 바꿔 기록 편집 배경(9:16), 프로필 사진(1:1)에서 함께 쓴다. 뒤로가기 제스처는 호출하는 쪽에서 [onBackClick]으로 연결한다
 *
 * @param imageUri        자를 이미지
 * @param aspectRatio     자르기 영역 비율
 * @param onBackClick     ◁ 탭 시 호출
 * @param onCropComplete  확인 후 자르기에 성공하면 자른 이미지 주소로 호출
 * @param onCropFailure   확인 후 자르기에 실패하면 호출
 */
@Composable
fun RouteeImageCropScreen(
    imageUri: Uri,
    aspectRatio: RouteeCropAspectRatio,
    onBackClick: () -> Unit,
    onCropComplete: (Uri) -> Unit,
    onCropFailure: (Throwable) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cropperState = rememberRouteeImageCropperState()
    val coroutineScope = rememberCoroutineScope()
    val currentOnCropComplete by rememberUpdatedState(onCropComplete)
    val currentOnCropFailure by rememberUpdatedState(onCropFailure)
    var isCropping by remember { mutableStateOf(false) }

    RouteeBgEllipse(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            RouteePageNavBar(
                onBackClick = onBackClick,
                actions = {
                    Text(
                        text = stringResource(R.string.ui_image_crop_confirm),
                        style = RouteeTheme.typography.labelSb18,
                        color = RouteeTheme.colors.white,
                        modifier = Modifier
                            .noRippleClickable(enabled = !isCropping, role = Role.Button) {
                                isCropping = true
                                coroutineScope.launch {
                                    cropperState.crop()
                                        .onSuccess { currentOnCropComplete(it) }
                                        .onFailure { currentOnCropFailure(it) }
                                    isCropping = false
                                }
                            }
                            .padding(
                                horizontal = RouteeTheme.spacing.Spacing16,
                                vertical = RouteeTheme.spacing.Spacing10,
                            ),
                    )
                },
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                RouteeImageCropper(
                    imageUri = imageUri,
                    aspectRatio = aspectRatio,
                    state = cropperState,
                    modifier = Modifier.fillMaxSize(),
                )
                RouteeImageRotationController(
                    onRotateLeftClick = cropperState::rotateLeft,
                    onResetClick = cropperState::reset,
                    onRotateRightClick = cropperState::rotateRight,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = RouteeTheme.spacing.Spacing6),
                )
            }
        }
    }
}

private class RouteeCropAspectRatioPreviewParameterProvider : PreviewParameterProvider<RouteeCropAspectRatio> {
    override val values = RouteeCropAspectRatio.entries.asSequence()
}

@PreviewDeviceSizes
@Composable
private fun RouteeImageCropScreenPreview(
    @PreviewParameter(RouteeCropAspectRatioPreviewParameterProvider::class) aspectRatio: RouteeCropAspectRatio,
) {
    RouteePreviewTheme {
        RouteeImageCropScreen(
            imageUri = Uri.EMPTY,
            aspectRatio = aspectRatio,
            onBackClick = {},
            onCropComplete = {},
            onCropFailure = {},
        )
    }
}
