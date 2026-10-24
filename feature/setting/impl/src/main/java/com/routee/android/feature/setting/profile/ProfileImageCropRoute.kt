package com.routee.android.feature.setting.profile

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.net.toUri
import androidx.navigation3.runtime.result.LocalResultEventBus
import com.routee.android.core.designsystem.component.RouteeCropAspectRatio
import com.routee.android.core.ui.imagecrop.RouteeImageCropScreen
import com.routee.android.feature.setting.profile.model.ProfileImageCropResult
import timber.log.Timber

/**
 * 프로필 사진 자르기 (RT-SET-002-4)
 *
 * ◁, 뒤로가기 제스처 시 앨범으로 돌아가고, 확인 시 자른 사진을 프로필 변경 화면 미리보기로 넘긴다
 */
@Composable
internal fun ProfileImageCropRoute(
    imageUri: String,
    onNavigateBack: () -> Unit,
) {
    val resultEventBus = LocalResultEventBus.current
    val uri = remember(imageUri) { imageUri.toUri() }
    val onBackClick = {
        resultEventBus.sendResult<ProfileImageCropResult>(ProfileImageCropResult.Cancel)
        onNavigateBack()
    }

    BackHandler(onBack = onBackClick)

    RouteeImageCropScreen(
        imageUri = uri,
        aspectRatio = RouteeCropAspectRatio.Square,
        onBackClick = onBackClick,
        onCropComplete = { croppedUri ->
            resultEventBus.sendResult<ProfileImageCropResult>(
                ProfileImageCropResult.Complete(croppedUri.toString()),
            )
            onNavigateBack()
        },
        onCropFailure = { Timber.e(it) },
    )
}
