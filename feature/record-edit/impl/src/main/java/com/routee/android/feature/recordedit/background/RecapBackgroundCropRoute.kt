package com.routee.android.feature.recordedit.background

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.net.toUri
import androidx.navigation3.runtime.result.LocalResultEventBus
import com.routee.android.core.designsystem.component.RouteeCropAspectRatio
import com.routee.android.core.ui.imagecrop.RouteeImageCropScreen
import timber.log.Timber

/**
 * 기록 편집 배경 자르기 (RT-EDIT-003-3)
 *
 * ◁, 뒤로가기 제스처 시 사진 선택으로 돌아가고, 확인 시 자른 사진을 편집 화면 배경으로 넘긴다
 */
@Composable
internal fun RecapBackgroundCropRoute(
    imageUri: String,
    onNavigateBack: () -> Unit,
) {
    val resultEventBus = LocalResultEventBus.current
    val uri = remember(imageUri) { imageUri.toUri() }
    val onBackClick = {
        resultEventBus.sendResult<RecapBackgroundCropResult>(RecapBackgroundCropResult.Cancel)
        onNavigateBack()
    }

    BackHandler(onBack = onBackClick)

    RouteeImageCropScreen(
        imageUri = uri,
        aspectRatio = RouteeCropAspectRatio.Story,
        onBackClick = onBackClick,
        onCropComplete = { croppedUri ->
            resultEventBus.sendResult<RecapBackgroundCropResult>(
                RecapBackgroundCropResult.Complete(croppedUri.toString()),
            )
            onNavigateBack()
        },
        onCropFailure = { Timber.e(it) },
    )
}
