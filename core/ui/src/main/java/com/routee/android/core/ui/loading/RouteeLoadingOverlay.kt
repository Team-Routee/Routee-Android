package com.routee.android.core.ui.loading

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
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
private const val SCREEN_HEIGHT = 800f
private const val ANIMATION_TOP = 220f
private const val ANIMATION_SIZE = 276f
private val AnimationSize = ANIMATION_SIZE.dp
private val MessageTopPadding = 217.dp
private val AnimationAlignment = BiasAlignment(
    horizontalBias = 0f,
    verticalBias = 2 * ANIMATION_TOP / (SCREEN_HEIGHT - ANIMATION_SIZE) - 1,
)

/**
 * 전역 로딩
 *
 * 화면 전체를 딤으로 덮고 로딩 애니메이션과 "로딩 중" 문구를 보여준다. 떠 있는 동안 터치와 뒤로가기는 막힌다.
 * 데이터를 불러오는 동안에만 호출한다
 */
@Composable
fun RouteeLoadingOverlay(modifier: Modifier = Modifier) {
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
        LoadingContent(modifier = modifier)
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(DesignSystemR.raw.loading))

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(RouteeTheme.colors.dimSecondary),
    ) {
        Box(
            modifier = Modifier
                .align(AnimationAlignment)
                .size(AnimationSize),
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
                    .padding(top = MessageTopPadding),
            )
        }
    }
}

@Preview(widthDp = 360, heightDp = 800)
@Composable
private fun RouteeLoadingOverlayPreview() {
    RouteeTheme {
        LoadingContent()
    }
}
