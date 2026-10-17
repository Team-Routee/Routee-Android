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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.routee.android.core.designsystem.R as DesignSystemR
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
 * 전역 로딩
 *
 * 화면 전체를 딤으로 덮고 로딩 애니메이션과 "로딩 중" 문구를 보여준다. 떠 있는 동안 터치와 뒤로가기는 막힌다.
 * 데이터를 불러오는 동안에만 호출한다
 *
 * @param showDelayMillis 호출 즉시 터치는 막고, 딤과 애니메이션은 이 시간이 지나야 보여준다. 빠른 응답에서 깜빡이지 않게 한다
 */
@Composable
fun RouteeLoadingOverlay(
    modifier: Modifier = Modifier,
    showDelayMillis: Long = DEFAULT_SHOW_DELAY_MILLIS,
) {
    var isContentVisible by remember { mutableStateOf(showDelayMillis <= 0) }

    LaunchedEffect(showDelayMillis) {
        delay(showDelayMillis)
        isContentVisible = true
    }

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        val window = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect { window?.setDimAmount(0f) }
        if (isContentVisible) {
            LoadingContent(modifier = modifier)
        } else {
            Box(modifier = modifier.fillMaxSize())
        }
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

@PreviewScreenSizes
@Composable
private fun RouteeLoadingOverlayPreview() {
    RouteeTheme {
        LoadingContent()
    }
}
