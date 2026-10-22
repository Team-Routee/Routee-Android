package com.routee.android.feature.recordedit.component.editor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.routee.android.core.designsystem.R as DesignSystemR
import com.routee.android.core.designsystem.component.RouteeBgEllipse
import com.routee.android.core.designsystem.preview.PreviewDeviceSizes
import com.routee.android.core.designsystem.theme.RouteePreviewTheme
import com.routee.android.core.ui.lottie.RouteeLottie

private val AnimationSize = 271.dp

/**
 * 기록 편집 완료 체크 애니메이션 (기록편집_완료 애니메이션)
 *
 * 화면 전체를 덮고 1회 재생한다. 재생 중에는 터치와 뒤로가기를 막는다
 *
 * @param onFinish 재생이 끝나면 호출
 */
@Composable
internal fun RecapCompleteAnimation(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler {}

    RouteeBgEllipse(
        modifier = modifier.pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) awaitPointerEvent()
            }
        },
    ) {
        RouteeLottie(
            resId = DesignSystemR.raw.lottie_check,
            onFinish = onFinish,
            modifier = Modifier
                .align(Alignment.Center)
                .size(AnimationSize),
        )
    }
}

@PreviewDeviceSizes
@Composable
private fun RecapCompleteAnimationPreview() {
    RouteePreviewTheme {
        RecapCompleteAnimation(onFinish = {})
    }
}
