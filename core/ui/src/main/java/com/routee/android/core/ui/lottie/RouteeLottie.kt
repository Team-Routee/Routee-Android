package com.routee.android.core.ui.lottie

import androidx.annotation.RawRes
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieAnimatable
import com.airbnb.lottie.compose.rememberLottieComposition
import com.routee.android.core.designsystem.R as DesignSystemR
import com.routee.android.core.designsystem.theme.RouteePreviewTheme

/**
 * 로띠 애니메이션
 *
 * @param resId          `res/raw` 로띠 파일 (`.json`, `.lottie`)
 * @param speed          재생 배속 (예: 1.5f)
 * @param durationMillis 이 시간에 맞춰 재생. null 이 아니면 [speed] 대신 사용
 * @param contentScale   영역에 맞추는 방식
 * @param onFinish       재생이 끝나면 호출
 */
@Composable
fun RouteeLottie(
    @RawRes resId: Int,
    modifier: Modifier = Modifier,
    speed: Float = 1f,
    durationMillis: Int? = null,
    contentScale: ContentScale = ContentScale.Fit,
    onFinish: (() -> Unit)? = null,
) {
    val currentOnFinish by rememberUpdatedState(onFinish)
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(resId))
    val animatable = rememberLottieAnimatable()

    LaunchedEffect(composition, speed, durationMillis) {
        val loaded = composition ?: return@LaunchedEffect
        animatable.animate(
            composition = loaded,
            iteration = 1,
            iterations = 1,
            speed = durationMillis?.let { loaded.duration / it } ?: speed,
        )
        currentOnFinish?.invoke()
    }

    LottieAnimation(
        composition = composition,
        progress = { animatable.progress },
        modifier = modifier,
        contentScale = contentScale,
    )
}

@Preview
@Composable
private fun RouteeLottiePreview() {
    RouteePreviewTheme {
        RouteeLottie(
            resId = DesignSystemR.raw.lottie_check,
            modifier = Modifier.size(120.dp),
        )
    }
}
