package com.routee.android.core.ui.loading

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.routee.android.core.designsystem.R as DesignSystemR
import com.routee.android.core.designsystem.preview.PreviewDeviceSizes
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.core.ui.R
import kotlinx.coroutines.delay

private const val SCREEN_HEIGHT = 800f
private const val ANIMATION_TOP = 220f
private const val ANIMATION_SIZE = 276f
private const val MESSAGE_TOP_RATIO = 217f / ANIMATION_SIZE
private const val DEFAULT_SHOW_DELAY_MILLIS = 300L
private val AnimationSize = ANIMATION_SIZE.dp
private val AnimationAlignment = BiasAlignment(
    horizontalBias = 0f,
    verticalBias = 2 * ANIMATION_TOP / (SCREEN_HEIGHT - ANIMATION_SIZE) - 1,
)

/**
 * 전역 로딩 표시 요청
 *
 * 데이터를 불러오는 동안에만 호출한다. 호출한 화면을 벗어나면 함께 내려간다. 화면은 앱 루트의 [RouteeLoadingHost]가 그린다
 */
@Composable
fun RouteeLoadingOverlay() {
    val state = LocalRouteeLoadingState.current

    DisposableEffect(state) {
        state.show()
        onDispose { state.hide() }
    }
}

/**
 * 전역 로딩 (RT-EDGE-001)
 *
 * 앱 루트에서 화면 전체 위에 둔다. 로딩 중에는 바로 터치를 막고, 딤과 애니메이션은 [showDelayMillis] 뒤에 보여준다.
 * 같은 창 안에 그려서 뒤로가기는 막지 않는다 (상세 화면은 이전 화면, 탭 첫 화면은 종료 토스트)
 *
 * @param showDelayMillis 빠른 응답에서 깜빡이지 않도록 딤과 애니메이션을 늦게 보여줄 시간
 */
@Composable
fun RouteeLoadingHost(
    state: RouteeLoadingState,
    modifier: Modifier = Modifier,
    showDelayMillis: Long = DEFAULT_SHOW_DELAY_MILLIS,
) {
    if (!state.isLoading) return

    var isContentVisible by remember { mutableStateOf(showDelayMillis <= 0) }

    LaunchedEffect(showDelayMillis) {
        delay(showDelayMillis)
        isContentVisible = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) awaitPointerEvent().changes.forEach { it.consume() }
                }
            },
    ) {
        if (isContentVisible) LoadingContent()
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(DesignSystemR.raw.lottie_loading))

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(RouteeTheme.colors.dimSecondary),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .align(AnimationAlignment)
                .sizeIn(maxWidth = AnimationSize, maxHeight = AnimationSize)
                .aspectRatio(1f),
        ) {
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.fillMaxSize(),
            )
            Text(
                text = stringResource(R.string.ui_loading_message),
                style = RouteeTheme.typography.labelM16,
                color = RouteeTheme.colors.white,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = maxHeight * MESSAGE_TOP_RATIO),
            )
        }
    }
}

@PreviewDeviceSizes
@Composable
private fun RouteeLoadingOverlayPreview() {
    RouteeTheme {
        LoadingContent()
    }
}
